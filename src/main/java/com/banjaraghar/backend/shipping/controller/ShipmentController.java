package com.banjaraghar.backend.shipping.controller;

import com.banjaraghar.backend.shipping.dto.response.ShipmentResponse;
import com.banjaraghar.backend.shipping.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders/{orderId}/shipment")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @GetMapping
    public ShipmentResponse get(@AuthenticationPrincipal UUID userId, @PathVariable UUID orderId) {
        return shipmentService.getForCustomer(userId, orderId);
    }
}