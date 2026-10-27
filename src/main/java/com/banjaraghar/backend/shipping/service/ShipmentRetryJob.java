package com.banjaraghar.backend.shipping.service;

import com.banjaraghar.backend.config.AppProperties;
import com.banjaraghar.backend.shipping.enums.ShipmentStatus;
import com.banjaraghar.backend.shipping.repo.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Every 30 seconds: books paid orders with Shiprocket, and retries the ones that failed earlier.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ShipmentRetryJob {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentService shipmentService;
    private final AppProperties appProperties;

    @Scheduled(fixedDelay = 30_000)
    public void bookDueShipments() {
        if (!appProperties.getShiprocket().isEnabled()) {
            return;
        }

        for (UUID orderId : shipmentRepository.findDueOrderIds(ShipmentStatus.PENDING, Instant.now())) {
            try {
                shipmentService.process(orderId);   // its own transaction per order
            } catch (Exception e) {
                log.error("Could not process the shipment of order {}", orderId, e);
            }
        }
    }
}
