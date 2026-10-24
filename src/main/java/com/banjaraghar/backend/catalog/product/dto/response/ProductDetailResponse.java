package com.banjaraghar.backend.catalog.product.dto.response;

import com.banjaraghar.backend.catalog.product.enums.StockStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ProductDetailResponse {

    private UUID id;
    private String name;
    private String slug;
    private String categoryName;
    private String categorySlug;
    private String description;
    private long price;        // paise
    private long mrp;          // paise
    private String material;
    private String colour;
    private String size;
    private String careInstructions;
    private StockStatus stockStatus;
    private List<ProductImageResponse> images;
}