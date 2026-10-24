package com.banjaraghar.backend.catalog.product.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeaturedRequest {

    @NotNull(message = "Featured flag is required")
    private Boolean featured;
}