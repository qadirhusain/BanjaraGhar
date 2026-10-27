package com.banjaraghar.backend.shipping.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "return_photos")
@Getter
@Setter
@NoArgsConstructor
public class ReturnPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "return_request_id", nullable = false)
    private ReturnRequest returnRequest;

    @Column(nullable = false)
    private String url;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
