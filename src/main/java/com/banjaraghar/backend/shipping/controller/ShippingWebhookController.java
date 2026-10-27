package com.banjaraghar.backend.shipping.controller;

import com.banjaraghar.backend.config.AppProperties;
import com.banjaraghar.backend.exception.UnauthorizedException;
import com.banjaraghar.backend.shipping.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

@RestController
@RequestMapping("/api/shipping/webhook")
@RequiredArgsConstructor
public class ShippingWebhookController {

    private final ShipmentService shipmentService;
    private final AppProperties appProperties;

    /** Shiprocket calls this for every tracking event. It must answer 200 once the event is accepted. */
    @PostMapping
    public ResponseEntity<Void> receive(@RequestHeader(value = "x-api-key", required = false) String token,
                                        @RequestBody Map<String, Object> payload) {
        String expected = appProperties.getShiprocket().getWebhookToken();
        if (expected == null || expected.isBlank() || token == null
                || !MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), token.getBytes(StandardCharsets.UTF_8))) {
            throw new UnauthorizedException("Invalid webhook token");
        }
        shipmentService.handleTracking(payload);
        return ResponseEntity.ok().build();
    }
}