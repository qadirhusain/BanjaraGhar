package com.banjaraghar.backend.order.service;

import com.banjaraghar.backend.settings.dto.response.ShippingSettingsResponse;
import com.banjaraghar.backend.settings.service.ShippingSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShippingFeeService {

    private final ShippingSettingsService shippingSettingsService;

    public long feeFor(long amountAfterDiscount) {
        ShippingSettingsResponse settings = shippingSettingsService.get();
        return amountAfterDiscount >= settings.getFreeAbove() ? 0 : settings.getFlatFee();
    }
}