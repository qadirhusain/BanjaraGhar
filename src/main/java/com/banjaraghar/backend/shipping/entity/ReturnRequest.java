package com.banjaraghar.backend.shipping.entity;

import com.banjaraghar.backend.order.entity.Order;
import com.banjaraghar.backend.shipping.enums.ReturnStatus;
import com.banjaraghar.backend.shipping.enums.ShipmentStatus;
import com.banjaraghar.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "return_requests")
@Getter
@Setter
@NoArgsConstructor
public class ReturnRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    private Order order;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReturnStatus status;

    @Column(nullable = false)
    private String reason;

    @Column(name = "admin_note")
    private String adminNote;

    @Column(name = "refund_amount")
    private Long refundAmount;

    @Column(name = "sr_order_id")
    private Long srOrderId;

    @Column(name = "sr_shipment_id")
    private Long srShipmentId;

    @Column(name = "return_awb")
    private String returnAwb;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "decided_at")
    private Instant decidedAt;
}
