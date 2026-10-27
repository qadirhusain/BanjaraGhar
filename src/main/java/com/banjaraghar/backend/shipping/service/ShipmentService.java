package com.banjaraghar.backend.shipping.service;

import com.banjaraghar.backend.catalog.product.entity.Product;
import com.banjaraghar.backend.config.AppProperties;
import com.banjaraghar.backend.exception.BadRequestException;
import com.banjaraghar.backend.exception.ConflictException;
import com.banjaraghar.backend.exception.ResourceNotFoundException;
import com.banjaraghar.backend.order.entity.Order;
import com.banjaraghar.backend.order.entity.OrderItem;
import com.banjaraghar.backend.order.enums.OrderStatus;
import com.banjaraghar.backend.order.repo.OrderRepository;
import com.banjaraghar.backend.shipping.client.Parcel;
import com.banjaraghar.backend.shipping.client.ShiprocketGateway;
import com.banjaraghar.backend.shipping.dto.response.AdminShipmentResponse;
import com.banjaraghar.backend.shipping.dto.response.ShipmentResponse;
import com.banjaraghar.backend.shipping.entity.Shipment;
import com.banjaraghar.backend.shipping.enums.ShipmentStatus;
import com.banjaraghar.backend.shipping.repo.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentService {

    private static final String TRACKING_URL = "https://shiprocket.co/tracking/";

    // Shiprocket shipment status codes (from their tracking docs)
    private static final int DELIVERED = 7;
    private static final Set<Integer> ON_THE_WAY = Set.of(6, 17, 18, 38, 42);
    private static final Set<Integer> RETURNING = Set.of(9, 10, 14, 40, 41, 46);

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final ShiprocketGateway gateway;
    private final AppProperties appProperties;

    // ------------------------------------------------------------ after payment

    @Transactional
    public Shipment queue(Order order) {
        return shipmentRepository.findByOrderId(order.getId()).orElseGet(() -> {
            Shipment shipment = new Shipment();
            shipment.setOrder(order);
            shipment.setStatus(ShipmentStatus.PENDING);
            shipment.setNextAttemptAt(Instant.now());
            return shipmentRepository.save(shipment);
        });
    }

    /**
     * Sends one waiting shipment to Shiprocket: create the order, pick the courier, request the pickup.
     * Each step is saved as soon as it works, so a retry carries on from where it stopped.
     * A failure never throws: it is recorded on the shipment and retried later with a growing delay.
     */
    @Transactional
    public void process(UUID orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId).orElse(null);
        Shipment shipment = shipmentRepository.findByOrderId(orderId).orElse(null);
        if (order == null || shipment == null || shipment.getStatus() != ShipmentStatus.PENDING) {
            return;
        }
        if (order.getStatus() != OrderStatus.PAID) {
            shipment.setStatus(ShipmentStatus.CANCELLED);   // canceled while it was waiting
            return;
        }

        try {
            if (shipment.getSrOrderId() == null) {
                ShiprocketGateway.SrOrder created = gateway.createOrder(order, buildParcel(order));
                shipment.setSrOrderId(created.getOrderId());
                shipment.setSrShipmentId(created.getShipmentId());
            }
            if (shipment.getAwbCode() == null) {
                ShiprocketGateway.SrAwb awb = gateway.assignAwb(shipment.getSrShipmentId());
                shipment.setAwbCode(awb.getAwbCode());
                shipment.setCourierName(awb.getCourierName());
            }
            gateway.requestPickup(shipment.getSrShipmentId());

            shipment.setStatus(ShipmentStatus.BOOKED);
            shipment.setLastError(null);
            log.info("Order {} booked with {} (AWB {})", order.getOrderNumber(), shipment.getCourierName(), shipment.getAwbCode());
        } catch (RuntimeException e) {
            recordFailure(order, shipment, e);
        }
    }

    private void recordFailure(Order order, Shipment shipment, RuntimeException e) {
        int attempts = shipment.getAttempts() + 1;
        shipment.setAttempts(attempts);
        String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        shipment.setLastError(message.length() <= 500 ? message : message.substring(0, 500));

        if (attempts >= appProperties.getShiprocket().getMaxAttempts()) {
            shipment.setStatus(ShipmentStatus.FAILED);
            log.error("Gave up booking order {} with Shiprocket after {} attempts: {}",
                    order.getOrderNumber(), attempts, message);
        } else {
            long delayMinutes = Math.min(1L << attempts, 60);   // 2, 4, 8, 16, 32, 60 ... minutes
            shipment.setNextAttemptAt(Instant.now().plusSeconds(delayMinutes * 60));
            log.warn("Booking order {} failed (attempt {}), retrying in {} min: {}",
                    order.getOrderNumber(), attempts, delayMinutes, message);
        }
    }

    // ------------------------------------------------------------ cancellation

    /**
     * Called when a paid order is canceled. The caller holds the order's row lock.
     * If Shiprocket refuses (for example the courier is already out for pickup), the exception makes the
     * whole cancellation roll back, so the customer is not refunded for a parcel that is still on its way.
     */
    @Transactional
    public void cancelForOrder(Order order) {
        Shipment shipment = shipmentRepository.findByOrderId(order.getId()).orElse(null);
        if (shipment == null || shipment.getStatus() == ShipmentStatus.CANCELLED) {
            return;
        }
        if (shipment.getAwbCode() != null) {
            gateway.cancelShipment(shipment.getAwbCode());
        }
        if (shipment.getSrOrderId() != null) {
            try {
                gateway.cancelOrder(shipment.getSrOrderId());
            } catch (RuntimeException e) {
                log.warn("Shipment of order {} is cancelled but its Shiprocket order is still open: {}",
                        order.getOrderNumber(), e.getMessage());
            }
        }
        shipment.setStatus(ShipmentStatus.CANCELLED);
    }

    // ------------------------------------------------------------ tracking webhook

    /** Handles one tracking event sent by Shiprocket. Unknown shipments are ignored. */
    @Transactional
    public void handleTracking(Map<String, Object> payload) {
        String awb = text(payload.get("awb"));
        if (awb == null) {
            return;
        }
        UUID orderId = shipmentRepository.findOrderIdByAwbCode(awb).orElse(null);
        if (orderId == null) {
            log.debug("Tracking event for unknown AWB {}", awb);
            return;
        }

        Order order = orderRepository.findByIdForUpdate(orderId).orElse(null);
        Shipment shipment = shipmentRepository.findByOrderId(orderId).orElse(null);
        if (order == null || shipment == null) {
            return;
        }

        String label = text(payload.get("current_status"));
        if (label != null) {
            shipment.setTrackingStatus(label.length() <= 100 ? label : label.substring(0, 100));
        }

        // The courier clearly has the parcel, so a booking that looked unfinished (lost response) was fine
        if (shipment.getStatus() == ShipmentStatus.PENDING || shipment.getStatus() == ShipmentStatus.FAILED) {
            shipment.setStatus(ShipmentStatus.BOOKED);
            shipment.setLastError(null);
        }

        Integer statusId = payload.get("shipment_status_id") instanceof Number n ? n.intValue() : null;
        String shipmentLabel = text(payload.get("shipment_status"));

        if (statusId != null && statusId == DELIVERED || "DELIVERED".equalsIgnoreCase(shipmentLabel)) {
            markDelivered(order);
        } else if (statusId != null && ON_THE_WAY.contains(statusId)) {
            markShipped(order);
        } else if (statusId != null && RETURNING.contains(statusId)) {
            log.warn("Order {} is on its way back to the seller (Shiprocket status {}). Check it in the admin panel",
                    order.getOrderNumber(), statusId);
        }
    }

    private void markShipped(Order order) {
        if (order.getStatus() == OrderStatus.PAID) {
            order.setStatus(OrderStatus.SHIPPED);
            order.setShippedAt(Instant.now());
        }
    }

    private void markDelivered(Order order) {
        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.SHIPPED) {
            if (order.getShippedAt() == null) {
                order.setShippedAt(Instant.now());
            }
            order.setStatus(OrderStatus.DELIVERED);
            order.setDeliveredAt(Instant.now());   // the return window starts here
        }
    }

    // ------------------------------------------------------------ admin

    /** Sends a failed or missing shipment to Shiprocket again right now. */
    @Transactional
    public AdminShipmentResponse retry(UUID orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (order.getStatus() != OrderStatus.PAID) {
            throw new BadRequestException("Only paid orders that have not shipped yet can be sent to the courier");
        }

        Shipment shipment = shipmentRepository.findByOrderId(orderId).orElseGet(() -> queue(order));
        if (shipment.getStatus() == ShipmentStatus.BOOKED) {
            throw new ConflictException("This order is already booked with the courier");
        }
        shipment.setStatus(ShipmentStatus.PENDING);
        shipment.setAttempts(0);
        shipment.setNextAttemptAt(Instant.now());

        process(orderId);
        return toAdminResponse(shipment);
    }

    @Transactional(readOnly = true)
    public AdminShipmentResponse getForAdmin(UUID orderId) {
        return toAdminResponse(findShipment(orderId));
    }

    @Transactional(readOnly = true)
    public ShipmentResponse getForCustomer(UUID userId, UUID orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        Shipment shipment = findShipment(orderId);
        return new ShipmentResponse(
                shipment.getStatus(),
                shipment.getAwbCode(),
                shipment.getCourierName(),
                shipment.getTrackingStatus(),
                shipment.getAwbCode() == null ? null : TRACKING_URL + shipment.getAwbCode(),
                order.getShippedAt(),
                order.getDeliveredAt());
    }

    private Shipment findShipment(UUID orderId) {
        return shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("This order has no shipment yet"));
    }

    private AdminShipmentResponse toAdminResponse(Shipment shipment) {
        return new AdminShipmentResponse(
                shipment.getStatus(),
                shipment.getAwbCode(),
                shipment.getCourierName(),
                shipment.getTrackingStatus(),
                shipment.getAwbCode() == null ? null : TRACKING_URL + shipment.getAwbCode(),
                shipment.getSrOrderId(),
                shipment.getSrShipmentId(),
                shipment.getAttempts(),
                shipment.getNextAttemptAt(),
                shipment.getLastError());
    }

    // ------------------------------------------------------------ parcel

    /**
     * One box per order: the longest and widest item sets the footprint and the heights are stacked.
     * This slightly overestimates multi-item orders, which is safer than a too-small box (Shiprocket
     * bills the weight it measures).
     */
    public Parcel buildParcel(Order order) {
        long grams = 0;
        int length = 1;
        int breadth = 1;
        int height = 0;
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            grams += (long) product.getPackedWeightG() * item.getQuantity();
            length = Math.max(length, product.getPackedLengthCm());
            breadth = Math.max(breadth, product.getPackedBreadthCm());
            height += product.getPackedHeightCm() * item.getQuantity();
        }
        return new Parcel(Math.max(grams, 100) / 1000.0, length, breadth, Math.max(height, 1));
    }

    private static String text(Object value) {
        return value == null || value.toString().isBlank() ? null : value.toString();
    }
}
