package com.banjaraghar.backend.catalog.category.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
public class CategoryRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    @Size(max = 500, message = "Image must be at most 500 characters")
    private String image;

    @PositiveOrZero(message = "Sort order cannot be negative")
    private Integer sortOrder;
}
