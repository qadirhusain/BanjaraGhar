package com.banjaraghar.backend.review.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VisibleRequest {

    @NotNull(message = "Visible flag is required")
    private Boolean visible;
}