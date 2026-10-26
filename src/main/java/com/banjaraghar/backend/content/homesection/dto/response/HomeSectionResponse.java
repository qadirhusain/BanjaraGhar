package com.banjaraghar.backend.content.homesection.dto.response;

import com.banjaraghar.backend.catalog.product.dto.response.ProductSummaryResponse;
import com.banjaraghar.backend.content.homesection.enums.SectionLayout;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

/** One product section of the home page, as the website and the app draw it. */
@Getter
@AllArgsConstructor
public class HomeSectionResponse {

    private UUID id;
    private String title;
    private String subtitle;
    private SectionLayout layout;
    private List<ProductSummaryResponse> products;
}