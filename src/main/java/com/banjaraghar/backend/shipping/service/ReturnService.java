package com.banjaraghar.backend.shipping.service;

import com.banjaraghar.backend.common.response.PageResponse;
import com.banjaraghar.backend.config.AppProperties;
import com.banjaraghar.backend.email.OrderEmailService;
import com.banjaraghar.backend.exception.BadRequestException;
import com.banjaraghar.backend.exception.ConflictException;
import com.banjaraghar.backend.exception.ResourceNotFoundException;
import com.banjaraghar.backend.order.entity.Order;
import com.banjaraghar.backend.order.enums.OrderStatus;
import com.banjaraghar.backend.order.enums.RefundStatus;
import com.banjaraghar.backend.order.repo.OrderRepository;
import com.banjaraghar.backend.payment.client.RazorpayGateway;
import com.banjaraghar.backend.shipping.client.Parcel;
import com.banjaraghar.backend.shipping.client.ShiprocketGateway;
import com.banjaraghar.backend.shipping.dto.request.ApproveReturnRequest;
import com.banjaraghar.backend.shipping.dto.request.RejectReturnRequest;
import com.banjaraghar.backend.shipping.dto.response.AdminReturnResponse;
import com.banjaraghar.backend.shipping.dto.response.ReturnResponse;
import com.banjaraghar.backend.shipping.entity.ReturnPhoto;
import com.banjaraghar.backend.shipping.entity.ReturnRequest;
import com.banjaraghar.backend.shipping.enums.ReturnStatus;
import com.banjaraghar.backend.shipping.repo.ReturnPhotoRepository;
import com.banjaraghar.backend.shipping.repo.ReturnRequestRepository;
import com.banjaraghar.backend.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReturnService {

    private static final int MAX_PHOTOS = 5;
    private static final int MAX_PAGE_SIZE = 50;
    private static final String STORAGE_FOLDER = "returns";

    private final ReturnRequestRepository returnRequestRepository;
    private final ReturnPhotoRepository returnPhotoRepository;
    private final OrderRepository orderRepository;
    private final FileStorageService fileStorageService;
    private final RazorpayGateway razorpayGateway;
    private final ShiprocketGateway shiprocketGateway;
    private final ShipmentService shipmentService;
    private final AppProperties appProperties;
    private final OrderEmailService orderEmailService;

    // ------------------------------------------------------------ customer

    @Transactional
    public ReturnResponse request(UUID userId, UUID orderId, String reason, List<MultipartFile> photos) {
        String cleanReason = reason == null ? "" : reason.trim();
        if (cleanReason.length() < 10 || cleanReason.length() > 1000) {
            throw new BadRequestException("Please describe the problem in 10 to 1000 characters");
        }
        List<MultipartFile> files = photos == null ? List.of() : photos.stream().filter(f -> !f.isEmpty()).toList();
        if (files.isEmpty() || files.size() > MAX_PHOTOS) {
            throw new BadRequestException("Please attach 1 to " + MAX_PHOTOS + " photos as proof");
        }

        // The row lock also stops a double tap from creating two requests
        Order order = orderRepository.findByIdForUpdate(orderId)
                .filter(found -> found.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.DELIVERED || order.getDeliveredAt() == null) {
            throw new BadRequestException("Returns can only be requested for delivered orders");
        }
        int windowDays = appProperties.getReturns().getWindowDays();
        if (Instant.now().isAfter(order.getDeliveredAt().plus(Duration.ofDays(windowDays)))) {
            throw new BadRequestException("The " + windowDays + " day return period for this order has ended");
        }
        if (returnRequestRepository.existsByOrderId(orderId)) {
            throw new ConflictException("A return has already been requested for this order");
        }

        List<String> urls = new ArrayList<>();
        try {
            ReturnRequest request = new ReturnRequest();
            request.setOrder(order);
            request.setUser(order.getUser());
            request.setStatus(ReturnStatus.REQUESTED);
            request.setReason(cleanReason);
            returnRequestRepository.save(request);

            List<ReturnPhoto> saved = new ArrayList<>();
            for (MultipartFile file : files) {
                String url = fileStorageService.storeImage(file, STORAGE_FOLDER);
                urls.add(url);

                ReturnPhoto photo = new ReturnPhoto();
                photo.setReturnRequest(request);
                photo.setUrl(url);
                photo.setSortOrder(saved.size());
                saved.add(photo);
            }
            returnPhotoRepository.saveAll(saved);
            return toResponse(request, urls);
        } catch (RuntimeException e) {
            urls.forEach(fileStorageService::delete);   // do not leave photos behind when the request fails
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public ReturnResponse getForCustomer(UUID userId, UUID orderId) {
        orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        ReturnRequest request = returnRequestRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("No return was requested for this order"));
        return toResponse(request, photoUrls(List.of(request)).getOrDefault(request.getId(), List.of()));
    }

    // ------------------------------------------------------------ activitylog

    @Transactional(readOnly = true)
    public PageResponse<AdminReturnResponse> list(ReturnStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ReturnRequest> result = status == null
                ? returnRequestRepository.findAll(pageable)
                : returnRequestRepository.findByStatus(status, pageable);

        Map<UUID, List<String>> photos = photoUrls(result.getContent());
        List<AdminReturnResponse> content = result.getContent().stream()
                .map(request -> toAdminResponse(request, photos.getOrDefault(request.getId(), List.of())))
                .toList();
        return PageResponse.of(result, content);
    }

    @Transactional(readOnly = true)
    public AdminReturnResponse get(UUID returnId) {
        ReturnRequest request = returnRequestRepository.findById(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("Return not found"));
        return toAdminResponse(request, photoUrls(List.of(request)).getOrDefault(returnId, List.of()));
    }

    /** Refunds the customer and, when Shiprocket is on, books the pickup of the parcel from the customer. */
    @Transactional
    public AdminReturnResponse approve(UUID returnId, ApproveReturnRequest body) {
        // Lock the order first (same order as everywhere else), then read the return fresh
        UUID orderId = returnRequestRepository.findOrderIdById(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("Return not found"));
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        ReturnRequest request = returnRequestRepository.findById(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("Return not found"));

        if (request.getStatus() != ReturnStatus.REQUESTED) {
            throw new ConflictException("This return has already been " + request.getStatus().name().toLowerCase());
        }
        if (order.getRefundId() != null) {
            throw new ConflictException("This order has already been refunded");
        }
        long amount = body.getRefundAmount();
        if (amount > order.getTotalAmount()) {
            throw new BadRequestException("The refund cannot be more than the order total");
        }

        // If Razorpay fails this throws and nothing is changed
        order.setRefundId(razorpayGateway.refund(order.getRazorpayPaymentId(), amount));
        order.setRefundStatus(RefundStatus.INITIATED);
        order.setRefundAmount(amount);

        request.setStatus(ReturnStatus.APPROVED);
        request.setRefundAmount(amount);
        request.setAdminNote(clean(body.getNote()));
        request.setDecidedAt(Instant.now());

        bookReversePickup(order, request);
        orderEmailService.returnApproved(order, request);

        return toAdminResponse(request, photoUrls(List.of(request)).getOrDefault(returnId, List.of()));
    }

    @Transactional
    public AdminReturnResponse reject(UUID returnId, RejectReturnRequest body) {
        UUID orderId = returnRequestRepository.findOrderIdById(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("Return not found"));
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        ReturnRequest request = returnRequestRepository.findById(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("Return not found"));

        if (request.getStatus() != ReturnStatus.REQUESTED) {
            throw new ConflictException("This return has already been " + request.getStatus().name().toLowerCase());
        }
        request.setStatus(ReturnStatus.REJECTED);
        request.setAdminNote(clean(body.getNote()));
        request.setDecidedAt(Instant.now());
        orderEmailService.returnRejected(order, request);

        return toAdminResponse(request, photoUrls(List.of(request)).getOrDefault(returnId, List.of()));
    }

    /**
     * Best effort: the refund is already decided, so a Shiprocket problem must not undo the approval.
     * If it fails, the activitylog books the pickup by hand in the Shiprocket panel (returnAwb stays empty).
     */
    private void bookReversePickup(Order order, ReturnRequest request) {
        if (!appProperties.getShiprocket().isEnabled()) {
            return;
        }
        try {
            Parcel parcel = shipmentService.buildParcel(order);
            ShiprocketGateway.SrOrder created = shiprocketGateway.createReturn(order, parcel);
            request.setSrOrderId(created.getOrderId());
            request.setSrShipmentId(created.getShipmentId());

            ShiprocketGateway.SrAwb awb = shiprocketGateway.assignAwb(created.getShipmentId());
            request.setReturnAwb(awb.getAwbCode());

            try {
                shiprocketGateway.requestPickup(created.getShipmentId());
            } catch (RuntimeException e) {
                log.warn("Return pickup request for order {} failed (it may be scheduled automatically): {}",
                        order.getOrderNumber(), e.getMessage());
            }
        } catch (RuntimeException e) {
            log.warn("Could not book the return pickup for order {}. Book it in the Shiprocket panel: {}",
                    order.getOrderNumber(), e.getMessage());
        }
    }

    // ------------------------------------------------------------ mapping

    private Map<UUID, List<String>> photoUrls(List<ReturnRequest> requests) {
        if (requests.isEmpty()) {
            return Map.of();
        }
        return returnPhotoRepository
                .findByReturnRequestIdInOrderBySortOrderAsc(requests.stream().map(ReturnRequest::getId).toList())
                .stream()
                .collect(Collectors.groupingBy(photo -> photo.getReturnRequest().getId(),
                        Collectors.mapping(ReturnPhoto::getUrl, Collectors.toList())));
    }

    private ReturnResponse toResponse(ReturnRequest request, List<String> photoUrls) {
        return new ReturnResponse(request.getId(), request.getStatus(), request.getReason(), request.getAdminNote(),
                request.getRefundAmount(), photoUrls, request.getCreatedAt(), request.getDecidedAt());
    }

    private AdminReturnResponse toAdminResponse(ReturnRequest request, List<String> photoUrls) {
        return new AdminReturnResponse(
                request.getId(),
                request.getOrder().getId(),
                request.getOrder().getOrderNumber(),
                request.getOrder().getTotalAmount(),
                request.getUser().getFullName(),
                request.getUser().getEmail(),
                request.getStatus(),
                request.getReason(),
                request.getAdminNote(),
                request.getRefundAmount(),
                request.getReturnAwb(),
                photoUrls,
                request.getCreatedAt(),
                request.getDecidedAt());
    }

    private static String clean(String note) {
        return note == null || note.isBlank() ? null : note.trim();
    }
}
