package com.banjaraghar.backend.coupon.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** What the customer would pay with this coupon. All amounts in paise. */
@Getter
@AllArgsConstructor
public class CouponCheckResponse {

    private String code;
    private String description;
    private long subtotal;       // available items in the cart
    private long discount;
    private long shippingFee;    // worked out after the discount
    private long total;          // subtotal - discount + shippingFee
}