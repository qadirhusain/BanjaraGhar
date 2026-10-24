package com.banjaraghar.backend.catalog.product.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ReorderImagesRequest {

    @NotEmpty(message = "Image list is required")
    private List<UUID> imageIds;
}