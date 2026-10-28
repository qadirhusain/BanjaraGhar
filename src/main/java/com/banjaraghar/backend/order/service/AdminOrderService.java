package com.banjaraghar.backend.order.service;

import com.banjaraghar.backend.common.response.PageResponse;
import com.banjaraghar.backend.exception.ResourceNotFoundException;
import com.banjaraghar.backend.order.dto.response.AdminOrderResponse;
import com.banjaraghar.backend.order.dto.response.AdminOrderSummaryResponse;
import com.banjaraghar.backend.order.entity.Order;
import com.banjaraghar.backend.order.entity.OrderItem;
import com.banjaraghar.backend.order.enums.OrderStatus;
import com.banjaraghar.backend.order.repo.OrderRepository;
import com.banjaraghar.backend.order.repo.OrderSpecifications;
import com.banjaraghar.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private static final int MAX_PAGE_SIZE = 50;

    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final OrderCancellationService cancellationService;

    @Transactional(readOnly = true)
    public PageResponse<AdminOrderSummaryResponse> list(OrderStatus status, String q, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Order> result = orderRepository.findAll(OrderSpecifications.forAdmin(status, q), pageable);
        Map<UUID, List<OrderItem>> items = orderService.loadItems(result.getContent());

        List<AdminOrderSummaryResponse> content = result.getContent().stream()
                .map(order -> toSummary(order, items.getOrDefault(order.getId(), List.of())))
                .toList();
        return PageResponse.of(result, content);
    }

    @Transactional(readOnly = true)
    public AdminOrderResponse get(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return toResponse(order);
    }

    /** The activitylog cancels an order (for example when an item turns out to be broken) and refunds it if it was paid. */
    @Transactional
    public AdminOrderResponse cancel(UUID orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        cancellationService.cancel(order);
        return toResponse(order);
    }

    private AdminOrderSummaryResponse toSummary(Order order, List<OrderItem> items) {
        User customer = order.getUser();
        return new AdminOrderSummaryResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getTotalAmount(),
                orderService.countUnits(items),
                customer.getFullName(),
                customer.getEmail(),
                order.getRefundStatus(),
                order.getCreatedAt(),
                order.getPaidAt());
    }

    private AdminOrderResponse toResponse(Order order) {
        User customer = order.getUser();
        return new AdminOrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getSubtotal(),
                order.getDiscountAmount(),
                order.getShippingFee(),
                order.getTotalAmount(),
                order.getCouponCode(),
                customer.getId(),
                customer.getFullName(),
                customer.getEmail(),
                customer.getPhone(),
                orderService.toAddressResponse(order),
                orderService.toItemResponses(order),
                order.getRazorpayOrderId(),
                order.getRazorpayPaymentId(),
                order.getRefundId(),
                order.getRefundStatus(),
                order.getRefundAmount(),
                order.getCreatedAt(),
                order.getPaidAt(),
                order.getCancelledAt());
    }
}