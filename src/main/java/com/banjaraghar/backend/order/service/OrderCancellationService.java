package com.banjaraghar.backend.order.service;

import com.banjaraghar.backend.exception.BadRequestException;
import com.banjaraghar.backend.exception.ResourceNotFoundException;
import com.banjaraghar.backend.order.dto.response.OrderResponse;
import com.banjaraghar.backend.order.entity.Order;
import com.banjaraghar.backend.order.enums.OrderStatus;
import com.banjaraghar.backend.order.enums.RefundStatus;
import com.banjaraghar.backend.order.repo.OrderRepository;
import com.banjaraghar.backend.payment.client.RazorpayGateway;
import com.banjaraghar.backend.shipping.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderCancellationService {

    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final RazorpayGateway razorpayGateway;
    private final ShipmentService shipmentService;

    /** A customer cancels their own order. */
    @Transactional
    public OrderResponse cancelByCustomer(UUID userId, UUID orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .filter(found -> found.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        cancel(order);
        return orderService.toResponse(order);
    }

    /**
     * Cancels an order and, if it was paid, refunds the full amount. Used for both customers and admins.
     * The caller must already hold the order's row lock (findByIdForUpdate).
     * If the Razorpay refund fails, the whole cancellation is rolled back and the order stays as it was.
     */
    @Transactional
    public void cancel(Order order) {
        switch (order.getStatus()) {
            case PENDING_PAYMENT ->
                // Nothing was paid yet, so there is nothing to refund. A payment that still arrives is refunded
                // by PaymentService because the order is no longer open.
                    orderService.release(order, OrderStatus.CANCELLED);
            case PAID -> {
                // Cancel the courier booking first: if Shiprocket refuses, nothing is canceled or refunded
                shipmentService.cancelForOrder(order);
                orderService.release(order, OrderStatus.CANCELLED);
                order.setRefundId(razorpayGateway.refund(order.getRazorpayPaymentId(), order.getTotalAmount()));
                order.setRefundStatus(RefundStatus.INITIATED);
                order.setRefundAmount(order.getTotalAmount());
            }
            case CANCELLED -> throw new BadRequestException("This order has already been cancelled");
            case EXPIRED -> throw new BadRequestException("This order has expired");
            case SHIPPED, DELIVERED ->
                    throw new BadRequestException("This order has already been shipped and can no longer be cancelled");
        }
    }
}