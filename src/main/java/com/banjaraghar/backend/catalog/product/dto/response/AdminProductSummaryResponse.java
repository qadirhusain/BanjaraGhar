package com.banjaraghar.backend.catalog.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class AdminProductSummaryResponse {

    private UUID id;
    private String name;
    private String slug;
    private String sku;
    private String categoryName;
    private long price;        // paise
    private int stockQuantity;
    private boolean featured;
    private boolean active;
    private String imageUrl;
}