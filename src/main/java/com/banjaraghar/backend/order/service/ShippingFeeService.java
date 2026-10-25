package com.banjaraghar.backend.order.service;

import com.banjaraghar.backend.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShippingFeeService {

    private final AppProperties appProperties;

    public long feeFor(long amountAfterDiscount) {
        AppProperties.Shipping shipping = appProperties.getShipping();
        return amountAfterDiscount >= shipping.getFreeAbove() ? 0 : shipping.getFlatFee();
    }
}
