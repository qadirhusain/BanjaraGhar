package com.banjaraghar.backend.catalog.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class AdminProductResponse {

    private UUID id;
    private UUID categoryId;
    private String name;
    private String slug;
    private String description;
    private long price;        // paise
    private long mrp;          // paise
    private String material;
    private String colour;
    private String size;
    private String careInstructions;
    private int stockQuantity;
    private boolean featured;
    private String sku;
    private int packedWeightG;
    private int packedLengthCm;
    private int packedBreadthCm;
    private int packedHeightCm;
    private boolean active;
    private Instant createdAt;
    private List<ProductImageResponse> images;
    private List<UUID> relatedProductIds;
}