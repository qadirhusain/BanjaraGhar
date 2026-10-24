package com.banjaraghar.backend.catalog.product.mapper;

import com.banjaraghar.backend.catalog.product.dto.response.*;
import com.banjaraghar.backend.catalog.product.entity.Product;
import com.banjaraghar.backend.catalog.product.entity.ProductImage;
import com.banjaraghar.backend.catalog.product.enums.StockStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ProductMapper {

    private static final int LOW_STOCK_THRESHOLD = 5;

    public StockStatus stockStatus(int quantity) {
        if (quantity <= 0) {
            return StockStatus.OUT_OF_STOCK;
        }
        return quantity <= LOW_STOCK_THRESHOLD ? StockStatus.LOW_STOCK : StockStatus.IN_STOCK;
    }

    public ProductSummaryResponse toSummary(Product product, String imageUrl) {
        return new ProductSummaryResponse(product.getId(),
                product.getName(),
                product.getSlug(),
                product.getPrice(),
                product.getMrp(),
                stockStatus(product.getStockQuantity()),
                product.isFeatured(),
                imageUrl);
    }

    public ProductDetailResponse toDetail(Product product, List<ProductImage> images) {
        return new ProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getCategory().getName(),
                product.getCategory().getSlug(),
                product.getDescription(),
                product.getPrice(),
                product.getMrp(),
                product.getMaterial(),
                product.getColour(),
                product.getSize(),
                product.getCareInstructions(),
                stockStatus(product.getStockQuantity()),
                images.stream().map(this::toImage).toList());
    }

    public AdminProductSummaryResponse toAdminSummary(Product product, String imageUrl) {
        return new AdminProductSummaryResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getSku(),
                product.getCategory().getName(),
                product.getPrice(),
                product.getStockQuantity(),
                product.isFeatured(),
                product.isActive(),
                imageUrl);
    }

    public AdminProductResponse toAdmin(Product product, List<ProductImage> images, List<UUID> relatedProductIds) {
        return new AdminProductResponse(
                product.getId(),
                product.getCategory().getId(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                product.getPrice(),
                product.getMrp(),
                product.getMaterial(),
                product.getColour(),
                product.getSize(),
                product.getCareInstructions(),
                product.getStockQuantity(),
                product.isFeatured(),
                product.getSku(),
                product.getPackedWeightG(),
                product.getPackedLengthCm(),
                product.getPackedBreadthCm(),
                product.getPackedHeightCm(),
                product.isActive(),
                product.getCreatedAt(),
                images.stream().map(this::toImage).toList(),
                relatedProductIds);
    }

    public ProductImageResponse toImage(ProductImage image) {
        return new ProductImageResponse(image.getId(), image.getUrl(), image.getSortOrder());
    }

}
