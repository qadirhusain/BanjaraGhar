package com.banjaraghar.backend.catalog.product.dto.response;

import com.banjaraghar.backend.catalog.product.enums.StockStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class ProductSummaryResponse {

    private UUID id;
    private String name;
    private String slug;
    private long price;        // paise
    private long mrp;          // paise
    private StockStatus stockStatus;
    private boolean featured;
    private String imageUrl;   // first image, null if the product has none
}