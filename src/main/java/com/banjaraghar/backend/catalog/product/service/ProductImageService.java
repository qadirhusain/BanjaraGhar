package com.banjaraghar.backend.catalog.product.service;

import com.banjaraghar.backend.catalog.product.dto.response.ProductImageResponse;
import com.banjaraghar.backend.catalog.product.entity.Product;
import com.banjaraghar.backend.catalog.product.entity.ProductImage;
import com.banjaraghar.backend.catalog.product.mapper.ProductMapper;
import com.banjaraghar.backend.catalog.product.repo.ProductImageRepository;
import com.banjaraghar.backend.catalog.product.repo.ProductRepository;
import com.banjaraghar.backend.exception.BadRequestException;
import com.banjaraghar.backend.exception.ResourceNotFoundException;
import com.banjaraghar.backend.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductImageService {

    private static final int MAX_IMAGES_PER_PRODUCT = 8;
    private static final String STORAGE_FOLDER = "products";

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final FileStorageService fileStorageService;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public List<ProductImage> getImages(UUID productId) {
        return productImageRepository.findByProductIdOrderBySortOrderAsc(productId);
    }

    @Transactional(readOnly = true)
    public Map<UUID, String> firstImageUrls(List<Product> products) {

        if (products.isEmpty()) {
            return Map.of();
        }
        List<UUID> productIds = products.stream().map(Product::getId).toList();
        Map<UUID, String> firstImages = new HashMap<>();
        productImageRepository.findByProductIdInOrderBySortOrderAsc(productIds)
                .forEach(image -> firstImages.putIfAbsent(image.getProduct().getId(), image.getUrl()));
        return firstImages;
    }

    @Transactional
    public ProductImageResponse upload(UUID productId, MultipartFile file) {
        Product product = getProduct(productId);

        int count = (int) productImageRepository.countByProductId(productId);
        if (count >= MAX_IMAGES_PER_PRODUCT) {
            throw new BadRequestException("A product can have up to " + MAX_IMAGES_PER_PRODUCT + " images");
        }

        String url = fileStorageService.storeImage(file, STORAGE_FOLDER);

        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setUrl(url);
        image.setSortOrder(count);   // new images go to the end
        return productMapper.toImage(productImageRepository.save(image));
    }

    @Transactional
    public List<ProductImageResponse> reorder(UUID productId, List<UUID> imageIds) {
        getProduct(productId);

        List<ProductImage> images = productImageRepository.findByProductIdOrderBySortOrderAsc(productId);
        Map<UUID, ProductImage> byId = images.stream()
                .collect(Collectors.toMap(ProductImage::getId, Function.identity()));

        if (imageIds.size() != images.size() || !byId.keySet().equals(new HashSet<>(imageIds))) {
            throw new BadRequestException("Send every image of this product exactly once");
        }

        for (int i = 0; i < imageIds.size(); i++) {
            byId.get(imageIds.get(i)).setSortOrder(i);
        }
        return imageIds.stream()
                .map(byId::get)
                .map(productMapper::toImage)
                .toList();
    }

    @Transactional
    public void delete(UUID productId, UUID imageId) {
        ProductImage image = productImageRepository.findByIdAndProductId(imageId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found"));
        productImageRepository.delete(image);
        fileStorageService.delete(image.getUrl());

        // Close the gap so sort orders stay 0, 1, 2... (the query below flushes the delete first)
        List<ProductImage> remaining = productImageRepository.findByProductIdOrderBySortOrderAsc(productId);
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setSortOrder(i);
        }
    }

    private Product getProduct(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }
}
