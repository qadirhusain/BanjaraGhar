package com.banjaraghar.backend.order.dto.response;

import com.banjaraghar.backend.order.enums.OrderStatus;
import com.banjaraghar.backend.order.enums.RefundStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/** One row of the activitylog order list. */
@Getter
@AllArgsConstructor
public class AdminOrderSummaryResponse {

    private UUID id;
    private String orderNumber;
    private OrderStatus status;
    private long totalAmount;           // paise
    private int itemCount;
    private String customerName;
    private String customerEmail;
    private RefundStatus refundStatus;
    private Instant createdAt;
    private Instant paidAt;
}