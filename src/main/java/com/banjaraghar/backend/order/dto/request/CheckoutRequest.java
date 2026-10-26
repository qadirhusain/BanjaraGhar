package com.banjaraghar.backend.order.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CheckoutRequest {

    @NotNull(message = "Delivery address is required")
    private UUID addressId;

    @Size(max = 30, message = "Invalid coupon code")
    private String couponCode;        // optional
}