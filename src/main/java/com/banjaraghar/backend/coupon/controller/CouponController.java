package com.banjaraghar.backend.coupon.controller;

import com.banjaraghar.backend.coupon.dto.request.CouponCheckRequest;
import com.banjaraghar.backend.coupon.dto.response.CouponCheckResponse;
import com.banjaraghar.backend.coupon.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/validate")
    public CouponCheckResponse validate(@AuthenticationPrincipal UUID userId,
                                        @Valid @RequestBody CouponCheckRequest request) {
        return couponService.check(userId, request.getCode());
    }
}