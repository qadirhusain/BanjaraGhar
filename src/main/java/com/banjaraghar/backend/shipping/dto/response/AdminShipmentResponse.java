package com.banjaraghar.backend.shipping.dto.response;

import com.banjaraghar.backend.shipping.enums.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class AdminShipmentResponse {

    private ShipmentStatus status;
    private String awbCode;
    private String courierName;
    private String trackingStatus;
    private String trackingUrl;
    private Long shiprocketOrderId;
    private Long shiprocketShipmentId;
    private int attempts;
    private Instant nextAttemptAt;
    private String lastError;
}