package com.banjaraghar.backend.catalog.product.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RelatedProductsRequest {

    // In display order. An empty list clears the picks.
    @NotNull(message = "Product list is required")
    @Size(max = 4, message = "You can pick up to 4 related products")
    private List<UUID> productIds;
}