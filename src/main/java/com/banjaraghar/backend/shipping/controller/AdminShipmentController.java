package com.banjaraghar.backend.shipping.controller;

import com.banjaraghar.backend.shipping.dto.response.AdminShipmentResponse;
import com.banjaraghar.backend.shipping.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/orders/{orderId}/shipment")
@RequiredArgsConstructor
public class AdminShipmentController {

    private final ShipmentService shipmentService;

    @GetMapping
    public AdminShipmentResponse get(@PathVariable UUID orderId) {
        return shipmentService.getForAdmin(orderId);
    }

    /** Sends a failed (or never queued) shipment to Shiprocket again right now. */
    @PostMapping("/retry")
    public AdminShipmentResponse retry(@PathVariable UUID orderId) {
        return shipmentService.retry(orderId);
    }
}