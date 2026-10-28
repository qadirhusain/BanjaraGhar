package com.banjaraghar.backend.settings.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "shipping_settings")
@Getter
@Setter
@NoArgsConstructor
public class ShippingSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "flat_fee", nullable = false)
    private long flatFee;

    @Column(name = "free_above", nullable = false)
    private long freeAbove;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}