package com.banjaraghar.backend.settings.controller;

import com.banjaraghar.backend.settings.dto.response.ShippingSettingsResponse;
import com.banjaraghar.backend.settings.service.ShippingSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final ShippingSettingsService shippingSettingsService;

    @GetMapping("/shipping")
    public ShippingSettingsResponse shipping() {
        return shippingSettingsService.get();
    }
}