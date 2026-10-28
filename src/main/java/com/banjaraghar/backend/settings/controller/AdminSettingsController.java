package com.banjaraghar.backend.settings.controller;

import com.banjaraghar.backend.settings.dto.request.ShippingSettingsRequest;
import com.banjaraghar.backend.settings.dto.response.ShippingSettingsResponse;
import com.banjaraghar.backend.settings.service.ShippingSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/activitylog/settings/shipping")
@RequiredArgsConstructor
public class AdminSettingsController {

    private final ShippingSettingsService shippingSettingsService;

    @GetMapping
    public ShippingSettingsResponse get() {
        return shippingSettingsService.get();
    }

    @PutMapping
    public ShippingSettingsResponse update(@Valid @RequestBody ShippingSettingsRequest request) {
        return shippingSettingsService.update(request);
    }
}