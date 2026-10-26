package com.banjaraghar.backend.payment.service;

import com.banjaraghar.backend.cart.repo.CartItemRepository;
import com.banjaraghar.backend.catalog.product.entity.Product;
import com.banjaraghar.backend.exception.BadRequestException;
import com.banjaraghar.backend.exception.ResourceNotFoundException;
import com.banjaraghar.backend.order.dto.response.OrderResponse;
import com.banjaraghar.backend.order.entity.Order;
import com.banjaraghar.backend.order.entity.OrderItem;
import com.banjaraghar.backend.order.enums.OrderStatus;
import com.banjaraghar.backend.order.enums.RefundStatus;
import com.banjaraghar.backend.order.repo.OrderRepository;
import com.banjaraghar.backend.order.service.OrderService;
import com.banjaraghar.backend.payment.client.RazorpayGateway;
import com.banjaraghar.backend.payment.dto.request.VerifyPaymentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final RazorpayGateway razorpayGateway;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final CartItemRepository cartItemRepository;

    @Transactional
    public OrderResponse verify(UUID userId, VerifyPaymentRequest request) {
        if (!razorpayGateway.isValidPaymentSignature(request.getRazorpayOrderId(), request.getRazorpayPaymentId(), request.getRazorpaySignature())) {
            throw new BadRequestException("Payment verification failed");
        }

        Order order = orderRepository.findByRazorpayOrderIdForUpdate(request.getRazorpayOrderId())
                .filter(found -> found.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        confirmPayment(order, request.getRazorpayPaymentId());
        return orderService.toResponse(order);
    }

    @Transactional
    public void handleWebhook(byte[] rawBody, String signature) {
        if (!razorpayGateway.isValidWebhookSignature(rawBody, signature)) {
            throw new BadRequestException("Invalid webhook signature");
        }

        try {
            JSONObject body = new JSONObject(new String(rawBody, StandardCharsets.UTF_8));
            String event = body.optString("event");

            switch (event) {
                case "payment.captured", "order.paid" -> handlePaid(body);
                case "payment.failed" -> log.info("Razorpay reported a failed payment attempt");
                case "refund.processed" -> handleRefund(body, RefundStatus.PROCESSED);
                case "refund.failed" -> handleRefund(body, RefundStatus.FAILED);
                default -> log.debug("Ignoring Razorpay webhook event {}", event);
            }
        } catch (JSONException e) {
            throw new BadRequestException("Malformed webhook body");
        }
    }

    private void handlePaid(JSONObject body) {
        JSONObject payment = body.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
        String razorpayOrderId = payment.optString("order_id");
        String paymentId = payment.optString("id");

        Order order = orderRepository.findByRazorpayOrderIdForUpdate(razorpayOrderId).orElse(null);
        if (order == null) {
            log.warn("Webhook for unknown Razorpay order {}", razorpayOrderId);
            return;
        }
        if (payment.optLong("amount", -1) != order.getTotalAmount()) {
            log.error("Webhook amount {} does not match order {} total {}",
                    payment.optLong("amount", -1), order.getOrderNumber(), order.getTotalAmount());
            return;
        }
        confirmPayment(order, paymentId);
    }

    private void handleRefund(JSONObject body, RefundStatus newStatus) {
        JSONObject refund = body.getJSONObject("payload").getJSONObject("refund").getJSONObject("entity");
        String refundId = refund.optString("id");

        // Not found means our own request has not been saved yet. Answering 404 makes Razorpay send the event again.
        Order order = orderRepository.findByRefundIdForUpdate(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund not found"));

        if (order.getRefundStatus() == RefundStatus.PROCESSED) {
            return;   // a finished refund never goes backwards, even if events arrive out of order
        }
        order.setRefundStatus(newStatus);

        if (newStatus == RefundStatus.FAILED) {
            log.error("Refund {} for order {} failed. Refund it manually from the Razorpay dashboard",
                    refundId, order.getOrderNumber());
        }
    }

    private void confirmPayment(Order order, String paymentId) {
        switch (order.getStatus()) {
            case PENDING_PAYMENT -> {
                order.setStatus(OrderStatus.PAID);
                order.setPaidAt(Instant.now());
                order.setRazorpayPaymentId(paymentId);

                List<UUID> productIds = order.getItems().stream()
                        .map(OrderItem::getProduct).map(Product::getId).toList();
                cartItemRepository.deleteByUserIdAndProductIdIn(order.getUser().getId(), productIds);
            }
            case EXPIRED, CANCELLED -> refundLatePayment(order, paymentId);
            default -> log.debug("Order {} is already {}", order.getOrderNumber(), order.getStatus());
        }
    }

    private void refundLatePayment(Order order, String paymentId) {
        if (order.getRefundId() != null) {
            return;   // already refunded
        }
        order.setRazorpayPaymentId(paymentId);
        order.setRefundId(razorpayGateway.refund(paymentId, order.getTotalAmount()));
        order.setRefundStatus(RefundStatus.INITIATED);
        order.setRefundAmount(order.getTotalAmount());
        log.warn("Payment {} arrived after order {} was {}. Refund started",
                paymentId, order.getOrderNumber(), order.getStatus());
    }
}
