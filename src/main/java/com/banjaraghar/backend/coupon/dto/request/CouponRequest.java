package com.banjaraghar.backend.coupon.dto.request;

import com.banjaraghar.backend.coupon.enums.DiscountType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class CouponRequest {

    @NotBlank(message = "Code is required")
    @Pattern(regexp = "^[A-Za-z0-9_-]{3,30}$",
            message = "Code must be 3 to 30 characters: letters, numbers, - or _")
    private String code;

    @Size(max = 200, message = "Description must be at most 200 characters")
    private String description;

    @NotNull(message = "Discount type is required")
    private DiscountType discountType;

    @NotNull(message = "Discount value is required")
    @Positive(message = "Discount value must be greater than 0")
    private Long discountValue;           // percent (1-100) or paise

    @Positive(message = "Maximum discount must be greater than 0")
    private Long maxDiscountAmount;       // paise, PERCENT coupons only

    @PositiveOrZero(message = "Minimum order amount cannot be negative")
    private Long minOrderAmount;          // paise, empty means no minimum

    @Positive(message = "Usage limit must be greater than 0")
    private Integer usageLimit;           // empty means unlimited

    @Positive(message = "Per-customer limit must be greater than 0")
    private Integer perUserLimit;         // empty means once per customer

    private Instant startsAt;             // empty means valid immediately

    private Instant expiresAt;            // empty means never expires
}