package com.banjaraghar.backend.settings.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShippingSettingsRequest {

    @NotNull(message = "Shipping fee is required")
    @PositiveOrZero(message = "Shipping fee cannot be negative")
    private Long flatFee;          // paise, e.g. 8900 = Rs 89

    @NotNull(message = "Free shipping threshold is required")
    @PositiveOrZero(message = "Free shipping threshold cannot be negative")
    private Long freeAbove;        // paise, e.g. 99900 = Rs 999
}