package com.banjaraghar.backend.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class OrderItemResponse {

    private UUID productId;
    private String name;
    private String imageUrl;
    private long unitPrice;     // paise
    private int quantity;
    private long lineTotal;     // paise
}