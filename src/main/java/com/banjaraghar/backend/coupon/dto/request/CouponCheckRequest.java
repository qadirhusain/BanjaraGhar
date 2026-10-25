package com.banjaraghar.backend.coupon.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CouponCheckRequest {

    @NotBlank(message = "Coupon code is required")
    @Size(max = 30, message = "Invalid coupon code")
    private String code;
}