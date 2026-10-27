package com.banjaraghar.backend.shipping.dto.response;

import com.banjaraghar.backend.shipping.enums.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class ShipmentResponse {

    private ShipmentStatus status;      // PENDING means "we are preparing it"
    private String awbCode;
    private String courierName;
    private String trackingStatus;
    private String trackingUrl;
    private Instant shippedAt;
    private Instant deliveredAt;
}