package com.banjaraghar.backend.order.service;

import com.banjaraghar.backend.catalog.product.repo.ProductRepository;
import com.banjaraghar.backend.common.response.PageResponse;
import com.banjaraghar.backend.coupon.repo.CouponRedemptionRepository;
import com.banjaraghar.backend.exception.ResourceNotFoundException;
import com.banjaraghar.backend.order.dto.response.OrderAddressResponse;
import com.banjaraghar.backend.order.dto.response.OrderItemResponse;
import com.banjaraghar.backend.order.dto.response.OrderResponse;
import com.banjaraghar.backend.order.dto.response.OrderSummaryResponse;
import com.banjaraghar.backend.order.entity.Order;
import com.banjaraghar.backend.order.entity.OrderItem;
import com.banjaraghar.backend.order.enums.OrderStatus;
import com.banjaraghar.backend.order.repo.OrderItemRepository;
import com.banjaraghar.backend.order.repo.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final int MAX_PAGE_SIZE = 50;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final CouponRedemptionRepository redemptionRepository;

    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> listOrders(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Order> result = orderRepository.findVisibleForUser(userId, OrderStatus.EXPIRED, pageable);
        Map<UUID, List<OrderItem>> items = loadItems(result.getContent());

        List<OrderSummaryResponse> content = result.getContent().stream()
                .map(order -> toSummary(order, items.getOrDefault(order.getId(), List.of())))
                .toList();
        return PageResponse.of(result, content);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID userId, UUID orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return toResponse(order);
    }

    /**
     * Puts the stock back and gives the coupon use back, then closes the order with the given status.
     * The caller must already hold the order's row lock (findByIdForUpdate or findByRazorpayOrderIdForUpdate).
     */
    @Transactional
    public void release(Order order, OrderStatus finalStatus) {
        order.getItems().stream()
                .sorted(Comparator.comparing(item -> item.getProduct().getId()))   // same order as checkout: no deadlocks
                .forEach(item -> productRepository.releaseStock(item.getProduct().getId(), item.getQuantity()));

        redemptionRepository.deleteByOrderId(order.getId());

        order.setStatus(finalStatus);
        if (finalStatus == OrderStatus.CANCELLED) {
            order.setCancelledAt(Instant.now());
        }
    }

    /**
     * Releases an order that is still waiting for payment.
     *
     * @param onlyIfPastExpiry true for the scheduled job (skip orders still inside the payment window),
     *                         false when a customer starts a fresh checkout
     * @return true if the order was released
     */
    @Transactional
    public boolean releaseUnpaid(UUID orderId, boolean onlyIfPastExpiry) {
        Order order = orderRepository.findByIdForUpdate(orderId).orElse(null);
        if (order == null || order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            return false;   // paid or released by someone else while we waited for the lock
        }
        if (onlyIfPastExpiry && order.getExpiresAt().isAfter(Instant.now())) {
            return false;
        }
        release(order, OrderStatus.EXPIRED);
        return true;
    }

    /** Loads the items of a whole page of orders with one query. */
    public Map<UUID, List<OrderItem>> loadItems(List<Order> orders) {
        if (orders.isEmpty()) {
            return Map.of();
        }
        return orderItemRepository.findByOrderIdIn(orders.stream().map(Order::getId).toList()).stream()
                .sorted(Comparator.comparing(OrderItem::getProductName))
                .collect(Collectors.groupingBy(item -> item.getOrder().getId()));
    }

    public int countUnits(List<OrderItem> items) {
        return items.stream().mapToInt(OrderItem::getQuantity).sum();
    }

    public OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getSubtotal(),
                order.getDiscountAmount(),
                order.getShippingFee(),
                order.getTotalAmount(),
                order.getCouponCode(),
                toAddressResponse(order),
                toItemResponses(order),
                order.getCreatedAt(),
                order.getPaidAt(),
                order.getExpiresAt(),
                order.getRefundStatus(),
                order.getRefundAmount());
    }

    public OrderAddressResponse toAddressResponse(Order order) {
        return new OrderAddressResponse(order.getShipName(), order.getShipPhone(), order.getShipLine1(),
                order.getShipLine2(), order.getShipCity(), order.getShipState(), order.getShipPincode());
    }

    public List<OrderItemResponse> toItemResponses(Order order) {
        return order.getItems().stream().map(this::toItemResponse).toList();
    }

    private OrderSummaryResponse toSummary(Order order, List<OrderItem> items) {
        OrderItem first = items.isEmpty() ? null : items.getFirst();
        return new OrderSummaryResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getTotalAmount(),
                countUnits(items),
                first == null ? null : first.getProductName(),
                first == null ? null : first.getImageUrl(),
                order.getCreatedAt(),
                order.getExpiresAt(),
                order.getRefundStatus());
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(item.getProduct().getId(), item.getProductName(), item.getImageUrl(),
                item.getUnitPrice(), item.getQuantity(), item.getLineTotal());
    }
}