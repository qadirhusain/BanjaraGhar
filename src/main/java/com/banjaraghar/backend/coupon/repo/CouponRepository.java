package com.banjaraghar.backend.coupon.repo;

import com.banjaraghar.backend.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface CouponRepository extends JpaRepository<Coupon, UUID> {

    Optional<Coupon> findByCode(String code);

    boolean existsByCode(String code);

    List<Coupon> findAllByOrderByCreatedAtDesc();
}
