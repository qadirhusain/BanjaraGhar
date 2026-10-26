package com.banjaraghar.backend.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/** Everything the website or iOS app needs to open the Razorpay payment screen. Amounts in paise. */
@Getter
@AllArgsConstructor
public class CheckoutResponse {

    private UUID orderId;
    private String orderNumber;
    private long subtotal;
    private long discountAmount;
    private long shippingFee;
    private long totalAmount;
    private String currency;
    private String razorpayKeyId;       // public key, safe to send to the client
    private String razorpayOrderId;
    private Instant expiresAt;          // pay before this time or the order is released
}