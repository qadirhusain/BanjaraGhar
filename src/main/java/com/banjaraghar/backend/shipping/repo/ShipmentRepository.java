package com.banjaraghar.backend.shipping.repo;

import com.banjaraghar.backend.shipping.entity.Shipment;
import com.banjaraghar.backend.shipping.enums.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {

    Optional<Shipment> findByOrderId(UUID orderId);

    @Query("select s.order.id from Shipment s where s.awbCode = :awb")
    Optional<UUID> findOrderIdByAwbCode(@Param("awb") String awb);

    @Query("select s.order.id from Shipment s where s.status = :status and s.nextAttemptAt <= :now order by s.nextAttemptAt")
    List<UUID> findDueOrderIds(@Param("status") ShipmentStatus status, @Param("now") Instant now);
}
