package com.banjaraghar.backend.coupon.repo;

import java.util.UUID;

/** One row of the "how many times was each coupon used" query. */
public interface CouponUsageCount {

    UUID getCouponId();

    long getTotal();
}