package com.banjaraghar.backend.catalog.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ProductRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must be at most 200 characters")
    private String name;

    @NotNull(message = "Category is required")
    private UUID categoryId;

    @NotBlank(message = "Description is required")
    @Size(max = 5000, message = "Description must be at most 5000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than zero")
    private Long price;          // paise

    @NotNull(message = "MRP is required")
    @Positive(message = "MRP must be greater than zero")
    private Long mrp;            // paise

    @Size(max = 150, message = "Material must be at most 150 characters")
    private String material;

    @Size(max = 150, message = "Colour must be at most 150 characters")
    private String colour;

    @Size(max = 100, message = "Size must be at most 100 characters")
    private String size;

    @Size(max = 255, message = "Care instructions must be at most 255 characters")
    private String careInstructions;

    @NotBlank(message = "SKU is required")
    @Size(max = 50, message = "SKU must be at most 50 characters")
    private String sku;

    @NotNull(message = "Packed weight is required")
    @Positive(message = "Packed weight must be greater than zero")
    private Integer packedWeightG;

    @NotNull(message = "Packed length is required")
    @Positive(message = "Packed length must be greater than zero")
    private Integer packedLengthCm;

    @NotNull(message = "Packed breadth is required")
    @Positive(message = "Packed breadth must be greater than zero")
    private Integer packedBreadthCm;

    @NotNull(message = "Packed height is required")
    @Positive(message = "Packed height must be greater than zero")
    private Integer packedHeightCm;
}