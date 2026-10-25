package com.banjaraghar.backend.coupon.entity;

import com.banjaraghar.backend.coupon.enums.DiscountType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "coupons")
@Getter
@Setter
@NoArgsConstructor
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String code;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType;

    /** Percentage (1-100) for PERCENT, paise for FIXED. */
    @Column(name = "discount_value", nullable = false)
    private long discountValue;

    /** Upper limit of the discount in paise. Only used by PERCENT coupons. */
    @Column(name = "max_discount_amount")
    private Long maxDiscountAmount;

    /** Smallest cart subtotal in paise that the coupon works on. */
    @Column(name = "min_order_amount", nullable = false)
    private long minOrderAmount;

    /** Total uses across all customers. Null means unlimited. */
    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "per_user_limit", nullable = false)
    private int perUserLimit;

    @Column(name = "starts_at")
    private Instant startsAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean active;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}