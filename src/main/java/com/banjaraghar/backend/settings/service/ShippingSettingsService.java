package com.banjaraghar.backend.settings.service;

import com.banjaraghar.backend.settings.dto.request.ShippingSettingsRequest;
import com.banjaraghar.backend.settings.dto.response.ShippingSettingsResponse;
import com.banjaraghar.backend.settings.entity.ShippingSettings;
import com.banjaraghar.backend.settings.repo.ShippingSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShippingSettingsService {

    private final ShippingSettingsRepository shippingSettingsRepository;

    @Transactional(readOnly = true)
    public ShippingSettingsResponse get() {
        return toResponse(load());
    }

    @Transactional
    public ShippingSettingsResponse update(ShippingSettingsRequest request) {
        ShippingSettings settings = load();
        settings.setFlatFee(request.getFlatFee());
        settings.setFreeAbove(request.getFreeAbove());
        return toResponse(shippingSettingsRepository.save(settings));
    }

    private ShippingSettings load() {
        return shippingSettingsRepository.findFirstBy()
                .orElseThrow(() -> new IllegalStateException("Shipping settings row is missing"));
    }

    private ShippingSettingsResponse toResponse(ShippingSettings settings) {
        return new ShippingSettingsResponse(settings.getFlatFee(), settings.getFreeAbove());
    }
}