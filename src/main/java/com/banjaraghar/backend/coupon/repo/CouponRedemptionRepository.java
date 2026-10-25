package com.banjaraghar.backend.coupon.repo;

import com.banjaraghar.backend.coupon.entity.CouponRedemption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.*;

public interface CouponRedemptionRepository extends JpaRepository<CouponRedemption, UUID> {

    long countByCouponId(UUID couponId);

    long countByCouponIdAndUserId(UUID couponId, UUID userId);

    @Query("select r.coupon.id as couponId, count(r) as total from CouponRedemption r group by r.coupon.id")
    List<CouponUsageCount> countUsagePerCoupon();

    void deleteByOrderId(UUID orderId);
}
