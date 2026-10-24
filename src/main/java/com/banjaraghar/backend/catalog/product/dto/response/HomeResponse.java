package com.banjaraghar.backend.catalog.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class HomeResponse {

    private List<ProductSummaryResponse> featured;
    private List<ProductSummaryResponse> newArrivals;
    private List<ProductSummaryResponse> bestSellers;
}