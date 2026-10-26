package com.banjaraghar.backend.order.enums;

public enum OrderStatus {
    PENDING_PAYMENT,   // stock is reserved, waiting for the customer to pay
    PAID,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    EXPIRED            // not paid in time; the stock was released
}
