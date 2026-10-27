package com.banjaraghar.backend.shipping.entity;

import com.banjaraghar.backend.order.entity.Order;
import com.banjaraghar.backend.shipping.enums.ShipmentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "shipments")
@Getter
@Setter
@NoArgsConstructor
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status;

    @Column(name = "sr_order_id")
    private Long srOrderId;

    @Column(name = "sr_shipment_id")
    private Long srShipmentId;

    @Column(name = "awb_code")
    private String awbCode;

    @Column(name = "courier_name")
    private String courierName;

    @Column(name = "tracking_status")
    private String trackingStatus;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "next_attempt_at", nullable = false)
    private Instant nextAttemptAt;

    @Column(name = "last_error")
    private String lastError;

    @CreationTimestamp
    private Instant createdAt;
}
