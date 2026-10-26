package com.banjaraghar.backend.content.homesection.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class SectionProductsRequest {

    // The full list, in the order they should appear. An empty list clears the section.
    @NotNull(message = "Product list is required")
    @Size(max = 20, message = "A section can hold at most 20 products")
    private List<@NotNull(message = "Product id cannot be empty") UUID> productIds;
}