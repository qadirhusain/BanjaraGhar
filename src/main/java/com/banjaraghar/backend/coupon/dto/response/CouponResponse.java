package com.banjaraghar.backend.coupon.dto.response;

import com.banjaraghar.backend.coupon.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/** Admin view of a coupon. */
@Getter
@AllArgsConstructor
public class CouponResponse {

    private UUID id;
    private String code;
    private String description;
    private DiscountType discountType;
    private long discountValue;         // percent or paise
    private Long maxDiscountAmount;     // paise
    private long minOrderAmount;        // paise
    private Integer usageLimit;
    private int perUserLimit;
    private Instant startsAt;
    private Instant expiresAt;
    private boolean active;
    private long redeemedCount;
    private Instant createdAt;
}