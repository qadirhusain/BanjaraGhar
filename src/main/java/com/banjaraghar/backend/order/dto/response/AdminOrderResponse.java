package com.banjaraghar.backend.order.dto.response;

import com.banjaraghar.backend.order.enums.OrderStatus;
import com.banjaraghar.backend.order.enums.RefundStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Everything the admin needs about one order, including the payment and refund references. */
@Getter
@AllArgsConstructor
public class AdminOrderResponse {

    private UUID id;
    private String orderNumber;
    private OrderStatus status;
    private long subtotal;              // paise
    private long discountAmount;
    private long shippingFee;
    private long totalAmount;
    private String couponCode;
    private UUID customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private OrderAddressResponse address;
    private List<OrderItemResponse> items;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String refundId;
    private RefundStatus refundStatus;
    private Long refundAmount;          // paise
    private Instant createdAt;
    private Instant paidAt;
    private Instant cancelledAt;
}