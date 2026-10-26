package com.banjaraghar.backend.order.dto.response;

import com.banjaraghar.backend.order.enums.OrderStatus;
import com.banjaraghar.backend.order.enums.RefundStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/** One row of the customer's "My orders" list. */
@Getter
@AllArgsConstructor
public class OrderSummaryResponse {

    private UUID id;
    private String orderNumber;
    private OrderStatus status;
    private long totalAmount;           // paise
    private int itemCount;              // units in the order
    private String firstItemName;       // for the list preview
    private String firstItemImageUrl;
    private Instant createdAt;
    private Instant expiresAt;          // only meaningful while the status is PENDING_PAYMENT
    private RefundStatus refundStatus;
}