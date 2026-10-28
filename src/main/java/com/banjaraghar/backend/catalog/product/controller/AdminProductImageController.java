package com.banjaraghar.backend.catalog.product.controller;

import com.banjaraghar.backend.catalog.product.dto.request.ReorderImagesRequest;
import com.banjaraghar.backend.catalog.product.dto.response.ProductImageResponse;
import com.banjaraghar.backend.catalog.product.service.ProductImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/activitylog/products/{productId}/images")
@RequiredArgsConstructor
public class AdminProductImageController {

    private final ProductImageService productImageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ProductImageResponse upload(@PathVariable UUID productId, @RequestParam("file") MultipartFile file) {
        return productImageService.upload(productId, file);
    }

    @PutMapping("/order")
    public List<ProductImageResponse> reorder(@PathVariable UUID productId,
                                              @Valid @RequestBody ReorderImagesRequest request) {
        return productImageService.reorder(productId, request.getImageIds());
    }

    @DeleteMapping("/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID productId, @PathVariable UUID imageId) {
        productImageService.delete(productId, imageId);
    }
}