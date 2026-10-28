package com.banjaraghar.backend.review.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateReviewRequest extends ReviewRequest {

    @NotNull(message = "Product is required")
    private UUID productId;
}