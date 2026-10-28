package com.banjaraghar.backend.coupon.controller;

import com.banjaraghar.backend.common.request.ActiveRequest;
import com.banjaraghar.backend.coupon.dto.request.CouponRequest;
import com.banjaraghar.backend.coupon.dto.response.CouponResponse;
import com.banjaraghar.backend.coupon.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/activitylog/coupons")
@RequiredArgsConstructor
public class AdminCouponController {

    private final CouponService couponService;

    @GetMapping
    public List<CouponResponse> list() {
        return couponService.listAll();
    }

    @GetMapping("/{id}")
    public CouponResponse get(@PathVariable UUID id) {
        return couponService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CouponResponse create(@Valid @RequestBody CouponRequest request) {
        return couponService.create(request);
    }

    @PutMapping("/{id}")
    public CouponResponse update(@PathVariable UUID id, @Valid @RequestBody CouponRequest request) {
        return couponService.update(id, request);
    }

    @PatchMapping("/{id}/active")
    public CouponResponse setActive(@PathVariable UUID id, @Valid @RequestBody ActiveRequest request) {
        return couponService.setActive(id, request.getActive());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        couponService.delete(id);
    }
}