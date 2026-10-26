package com.banjaraghar.backend.order.dto.response;

import com.banjaraghar.backend.order.enums.OrderStatus;
import com.banjaraghar.backend.order.enums.RefundStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class OrderResponse {

    private UUID id;
    private String orderNumber;
    private OrderStatus status;
    private long subtotal;          // paise
    private long discountAmount;
    private long shippingFee;
    private long totalAmount;
    private String couponCode;
    private OrderAddressResponse address;
    private List<OrderItemResponse> items;
    private Instant createdAt;
    private Instant paidAt;
    private Instant expiresAt;      // only meaningful while the status is PENDING_PAYMENT
    private RefundStatus refundStatus;
    private Long refundAmount;      // paise
}