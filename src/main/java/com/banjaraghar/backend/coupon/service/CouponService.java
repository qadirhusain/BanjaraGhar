package com.banjaraghar.backend.coupon.service;

import com.banjaraghar.backend.cart.service.CartService;
import com.banjaraghar.backend.coupon.dto.request.CouponRequest;
import com.banjaraghar.backend.coupon.dto.response.CouponCheckResponse;
import com.banjaraghar.backend.coupon.dto.response.CouponResponse;
import com.banjaraghar.backend.coupon.entity.Coupon;
import com.banjaraghar.backend.coupon.entity.CouponRedemption;
import com.banjaraghar.backend.coupon.enums.DiscountType;
import com.banjaraghar.backend.coupon.repo.CouponRedemptionRepository;
import com.banjaraghar.backend.coupon.repo.CouponRepository;
import com.banjaraghar.backend.coupon.repo.CouponUsageCount;
import com.banjaraghar.backend.exception.BadRequestException;
import com.banjaraghar.backend.exception.ConflictException;
import com.banjaraghar.backend.exception.ResourceNotFoundException;
import com.banjaraghar.backend.order.service.ShippingFeeService;
import com.banjaraghar.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponRedemptionRepository couponRedemptionRepository;
    private final CartService cartService;
    private final ShippingFeeService shippingFeeService;

    // ---------------------------------------------------------------- customer

    @Transactional(readOnly = true)
    public CouponCheckResponse check(UUID userId, String code) {
        long subtotal = cartService.getCart(userId).getSubtotal();
        if (subtotal <= 0) {
            throw new BadRequestException("Your cart is empty");
        }

        Coupon coupon = getUsableCoupon(userId, code, subtotal);
        long discount = calculateDiscount(coupon, subtotal);
        long shippingFee = shippingFeeService.feeFor(subtotal - discount);

        return new CouponCheckResponse(coupon.getCode(), coupon.getDescription(), subtotal, discount,
                shippingFee, subtotal - discount + shippingFee);
    }

    @Transactional(readOnly = true)
    public Coupon getUsableCoupon(UUID userId, String code, long subtotal) {
        Coupon coupon = couponRepository.findByCode(normalize(code))
                .orElseThrow(() -> new BadRequestException("Invalid coupon code"));
        checkRules(coupon, userId, subtotal);
        return coupon;
    }

    @Transactional
    public Coupon lockUsableCoupon(UUID userId, String code, long subtotal) {
        Coupon coupon = couponRepository.findByCodeForUpdate(normalize(code))
                .orElseThrow(() -> new BadRequestException("Invalid coupon code"));
        checkRules(coupon, userId, subtotal);
        return coupon;
    }

    @Transactional
    public void recordRedemption(Coupon coupon, User user, UUID orderId) {
        CouponRedemption redemption = new CouponRedemption();
        redemption.setCoupon(coupon);
        redemption.setUser(user);
        redemption.setOrderId(orderId);
        couponRedemptionRepository.save(redemption);
    }

    public long calculateDiscount(Coupon coupon, long subtotal) {
        long discount;
        if (coupon.getDiscountType() == DiscountType.PERCENT) {
            discount = subtotal * coupon.getDiscountValue() / 100;
            if (coupon.getMaxDiscountAmount() != null) {
                discount = Math.min(discount, coupon.getMaxDiscountAmount());
            }
        } else {
            discount = coupon.getDiscountValue();
        }
        return Math.min(discount, subtotal);
    }

    // ------------------------------------------------------------------- admin

    @Transactional(readOnly = true)
    public List<CouponResponse> listAll() {
        Map<UUID, Long> usage = couponRedemptionRepository.countUsagePerCoupon().stream()
                .collect(Collectors.toMap(CouponUsageCount::getCouponId, CouponUsageCount::getTotal));
        return couponRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(coupon -> toResponse(coupon, usage.getOrDefault(coupon.getId(), 0L)))
                .toList();
    }

    @Transactional(readOnly = true)
    public CouponResponse get(UUID id) {
        Coupon coupon = getCoupon(id);
        return toResponse(coupon, couponRedemptionRepository.countByCouponId(id));
    }

    @Transactional
    public CouponResponse create(CouponRequest request) {
        String code = normalize(request.getCode());
        if (couponRepository.existsByCode(code)) {
            throw new ConflictException("A coupon with this code already exists");
        }

        Coupon coupon = new Coupon();
        coupon.setCode(code);
        coupon.setActive(true);
        apply(coupon, request);
        return toResponse(couponRepository.save(coupon), 0);
    }

    @Transactional
    public CouponResponse update(UUID id, CouponRequest request) {
        Coupon coupon = getCoupon(id);
        if (!coupon.getCode().equals(normalize(request.getCode()))) {
            throw new BadRequestException("A coupon code cannot be changed. Create a new coupon instead");
        }

        apply(coupon, request);
        return toResponse(couponRepository.save(coupon), couponRedemptionRepository.countByCouponId(id));
    }

    @Transactional
    public CouponResponse setActive(UUID id, boolean active) {
        Coupon coupon = getCoupon(id);
        coupon.setActive(active);
        return toResponse(couponRepository.save(coupon), couponRedemptionRepository.countByCouponId(id));
    }

    @Transactional
    public void delete(UUID id) {
        Coupon coupon = getCoupon(id);
        if (couponRedemptionRepository.countByCouponId(id) > 0) {
            throw new ConflictException("This coupon has been used. Deactivate it instead of deleting it");
        }
        couponRepository.delete(coupon);
    }

    // ----------------------------------------------------------------- helpers

    private Coupon getCoupon(UUID id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
    }

    private void apply(Coupon coupon, CouponRequest request) {
        DiscountType discountType = request.getDiscountType();
        long value = request.getDiscountValue();

        if (discountType == DiscountType.PERCENT && value > 100) {
            throw new BadRequestException("A percentage discount cannot be more than 100");
        }
        if (request.getStartsAt() != null && request.getExpiresAt() != null
                && !request.getStartsAt().isBefore(request.getExpiresAt())) {
            throw new BadRequestException("The start date must be before the expiry date");
        }

        coupon.setDescription(blankToNull(request.getDescription()));
        coupon.setDiscountType(discountType);
        coupon.setDiscountValue(value);
        // A maximum discount only makes sense for percentage coupons
        coupon.setMaxDiscountAmount(discountType == DiscountType.PERCENT ? request.getMaxDiscountAmount() : null);
        coupon.setMinOrderAmount(request.getMinOrderAmount() == null ? 0 : request.getMinOrderAmount());
        coupon.setUsageLimit(request.getUsageLimit());
        coupon.setPerUserLimit(request.getPerUserLimit() == null ? 1 : request.getPerUserLimit());
        coupon.setStartsAt(request.getStartsAt());
        coupon.setExpiresAt(request.getExpiresAt());
    }

    private String normalize(String code) {
        return code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String formatRupees(long paise) {
        return paise % 100 == 0 ? String.valueOf(paise / 100) : String.format(Locale.ROOT, "%.2f", paise / 100.0);
    }

    private CouponResponse toResponse(Coupon coupon, long redeemCount) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getCode(),
                coupon.getDescription(),
                coupon.getDiscountType(),
                coupon.getDiscountValue(),
                coupon.getMaxDiscountAmount(),
                coupon.getMinOrderAmount(),
                coupon.getUsageLimit(),
                coupon.getPerUserLimit(),
                coupon.getStartsAt(),
                coupon.getExpiresAt(),
                coupon.isActive(),
                redeemCount,
                coupon.getCreatedAt());
    }

    private void checkRules(Coupon coupon, UUID userId, long subtotal) {
        if (!coupon.isActive()) {
            throw new BadRequestException("Invalid coupon code");
        }

        Instant now = Instant.now();
        if (coupon.getStartsAt() != null && now.isBefore(coupon.getStartsAt())) {
            throw new BadRequestException("This coupon is not active yet");
        }
        if (coupon.getExpiresAt() != null && !now.isBefore(coupon.getExpiresAt())) {
            throw new BadRequestException("This coupon has expired");
        }
        if (subtotal < coupon.getMinOrderAmount()) {
            throw new BadRequestException("This coupon needs a minimum order of ₹"
                    + formatRupees(coupon.getMinOrderAmount()));
        }
        if (coupon.getUsageLimit() != null
                && couponRedemptionRepository.countByCouponId(coupon.getId()) >= coupon.getUsageLimit()) {
            throw new BadRequestException("This coupon has reached its usage limit");
        }
        if (couponRedemptionRepository.countByCouponIdAndUserId(coupon.getId(), userId) >= coupon.getPerUserLimit()) {
            throw new BadRequestException("You have already used this coupon");
        }
    }
}
