package com.banjaraghar.backend.order.repo;

import com.banjaraghar.backend.order.entity.Order;
import com.banjaraghar.backend.order.enums.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {

    @EntityGraph(attributePaths = "items")
    Optional<Order> findByIdAndUserId(UUID id, UUID userId);

    List<Order> findByUserIdAndStatus(UUID userId, OrderStatus status);

    /** The customer's orders. Orders that expired unpaid never really happened, so they are hidden. */
    @Query("select o from Order o where o.user.id = :userId and (o.status <> :expired or o.refundId is not null)")
    Page<Order> findVisibleForUser(@Param("userId") UUID userId, @Param("expired") OrderStatus expired,
                                   Pageable pageable);

    // Admin list: loads the customer together with the page, avoiding one query per row
    @Override
    @EntityGraph(attributePaths = "user")
    Page<Order> findAll(Specification<Order> spec, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findByIdForUpdate(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.razorpayOrderId = :razorpayOrderId")
    Optional<Order> findByRazorpayOrderIdForUpdate(@Param("razorpayOrderId") String razorpayOrderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.refundId = :refundId")
    Optional<Order> findByRefundIdForUpdate(@Param("refundId") String refundId);

    @Query("select o.id from Order o where o.status = :status and o.expiresAt < :now")
    List<UUID> findIdsByStatusAndExpiresBefore(@Param("status") OrderStatus status, @Param("now") Instant now);

    @Query(value = "select nextval('order_number_seq')", nativeQuery = true)
    long nextOrderNumber();
}