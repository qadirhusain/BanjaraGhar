package com.banjaraghar.backend.catalog.product.controller;

import com.banjaraghar.backend.catalog.product.dto.response.ProductDetailResponse;
import com.banjaraghar.backend.catalog.product.dto.response.ProductSummaryResponse;
import com.banjaraghar.backend.catalog.product.enums.ProductSort;
import com.banjaraghar.backend.catalog.product.service.ProductService;
import com.banjaraghar.backend.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // minPrice and maxPrice are in paise, like every price in the API
    @GetMapping
    public PageResponse<ProductSummaryResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long minPrice,
            @RequestParam(required = false) Long maxPrice,
            @RequestParam(defaultValue = "false") boolean inStock,
            @RequestParam(defaultValue = "NEWEST") ProductSort sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return productService.search(q, category, minPrice, maxPrice, inStock, sort, page, size);
    }

    @GetMapping("/{slug}")
    public ProductDetailResponse getBySlug(@PathVariable String slug) {
        return productService.getBySlug(slug);
    }

    @GetMapping("/{slug}/related")
    public List<ProductSummaryResponse> getRelated(@PathVariable String slug) {
        return productService.getRelated(slug);
    }
}