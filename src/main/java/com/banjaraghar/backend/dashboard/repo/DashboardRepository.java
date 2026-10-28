package com.banjaraghar.backend.dashboard.repo;

import com.banjaraghar.backend.order.enums.OrderStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/** Read-only counts and sums for the admin dashboard. */
@Repository
public class DashboardRepository {

    // Orders that count as sales: paid, on the way or delivered
    private static final List<OrderStatus> SOLD = List.of(OrderStatus.PAID, OrderStatus.SHIPPED, OrderStatus.DELIVERED);

    @PersistenceContext
    private EntityManager entityManager;

    public long salesSince(Instant from) {
        return single("""
                select coalesce(sum(o.totalAmount - coalesce(o.refundAmount, 0L)), 0L) from Order o
                where o.status in :sold and o.paidAt >= :from
                """, Map.of("sold", SOLD, "from", from));
    }

    public long ordersSince(Instant from) {
        return single("select count(o) from Order o where o.status in :sold and o.paidAt >= :from",
                Map.of("sold", SOLD, "from", from));
    }

    public long ordersToShip() {
        return single("select count(o) from Order o where o.status = com.banjaraghar.backend.order.enums.OrderStatus.PAID",
                Map.of());
    }

    public long shipmentsNeedingAttention() {
        return single("select count(s) from Shipment s where s.status = com.banjaraghar.backend.shipping.enums.ShipmentStatus.FAILED",
                Map.of());
    }

    public long returnsAwaitingReview() {
        return single("select count(r) from ReturnRequest r where r.status = com.banjaraghar.backend.shipping.enums.ReturnStatus.REQUESTED",
                Map.of());
    }

    public long newMessages() {
        return single("select count(m) from ContactMessage m where m.status = com.banjaraghar.backend.contact.enums.MessageStatus.NEW",
                Map.of());
    }

    public long lowStockProducts(int threshold) {
        return single("select count(p) from Product p where p.active = true and p.stockQuantity between 1 and :threshold",
                Map.of("threshold", threshold));
    }

    public long outOfStockProducts() {
        return single("select count(p) from Product p where p.active = true and p.stockQuantity = 0", Map.of());
    }

    public long customers() {
        return single("select count(u) from User u where u.role = com.banjaraghar.backend.user.enums.Role.CUSTOMER and u.deletedAt is null",
                Map.of());
    }

    public long activeSubscribers() {
        return single("select count(n) from NewsletterSubscriber n where n.active = true", Map.of());
    }

    private long single(String jpql, Map<String, Object> parameters) {
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        parameters.forEach(query::setParameter);
        return query.getSingleResult();
    }
}