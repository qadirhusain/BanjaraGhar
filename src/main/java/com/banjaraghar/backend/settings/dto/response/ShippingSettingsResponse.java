package com.banjaraghar.backend.settings.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ShippingSettingsResponse {

    private long flatFee;
    private long freeAbove;
}