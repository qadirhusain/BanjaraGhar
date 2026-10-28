package com.banjaraghar.backend.catalog.product.controller;

import com.banjaraghar.backend.catalog.product.dto.request.FeaturedRequest;
import com.banjaraghar.backend.catalog.product.dto.request.ProductRequest;
import com.banjaraghar.backend.catalog.product.dto.request.RelatedProductsRequest;
import com.banjaraghar.backend.catalog.product.dto.request.StockRequest;
import com.banjaraghar.backend.catalog.product.dto.response.AdminProductResponse;
import com.banjaraghar.backend.catalog.product.dto.response.AdminProductSummaryResponse;
import com.banjaraghar.backend.catalog.product.service.AdminProductService;
import com.banjaraghar.backend.common.request.ActiveRequest;
import com.banjaraghar.backend.common.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/activitylog/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductService adminProductService;

    @GetMapping
    public PageResponse<AdminProductSummaryResponse> list(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "20") int size) {
        return adminProductService.list(page, size);
    }

    @GetMapping("/{id}")
    public AdminProductResponse get(@PathVariable UUID id) {
        return adminProductService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminProductResponse create(@Valid @RequestBody ProductRequest request) {
        return adminProductService.create(request);
    }

    @PutMapping("/{id}")
    public AdminProductResponse update(@PathVariable UUID id, @Valid @RequestBody ProductRequest request) {
        return adminProductService.update(id, request);
    }

    @PatchMapping("/{id}/active")
    public AdminProductResponse setActive(@PathVariable UUID id, @Valid @RequestBody ActiveRequest request) {
        return adminProductService.setActive(id, request.getActive());
    }

    @PatchMapping("/{id}/stock")
    public AdminProductResponse setStock(@PathVariable UUID id, @Valid @RequestBody StockRequest request) {
        return adminProductService.setStock(id, request.getQuantity());
    }

    @PatchMapping("/{id}/featured")
    public AdminProductResponse setFeatured(@PathVariable UUID id, @Valid @RequestBody FeaturedRequest request) {
        return adminProductService.setFeatured(id, request.getFeatured());
    }

    @PutMapping("/{id}/related")
    public AdminProductResponse setRelated(@PathVariable UUID id,
                                           @Valid @RequestBody RelatedProductsRequest request) {
        return adminProductService.setRelated(id, request);
    }
}