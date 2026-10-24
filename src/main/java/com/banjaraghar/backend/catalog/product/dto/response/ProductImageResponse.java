package com.banjaraghar.backend.catalog.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class ProductImageResponse {

    private UUID id;
    private String url;
    private int sortOrder;
}