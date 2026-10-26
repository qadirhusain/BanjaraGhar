package com.banjaraghar.backend.order.entity;

import com.banjaraghar.backend.order.enums.OrderStatus;
import com.banjaraghar.backend.order.enums.RefundStatus;
import com.banjaraghar.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_number", nullable = false, updatable = false)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    /** Items total in paise, before the coupon. */
    @Column(nullable = false)
    private long subtotal;

    @Column(name = "discount_amount", nullable = false)
    private long discountAmount;

    @Column(name = "shipping_fee", nullable = false)
    private long shippingFee;

    /** What the customer pays: subtotal - discount + shipping, in paise. */
    @Column(name = "total_amount", nullable = false)
    private long totalAmount;

    @Column(name = "coupon_code")
    private String couponCode;

    @Column(name = "ship_name", nullable = false)
    private String shipName;

    @Column(name = "ship_phone", nullable = false)
    private String shipPhone;

    @Column(name = "ship_line1", nullable = false)
    private String shipLine1;

    @Column(name = "ship_line2")
    private String shipLine2;

    @Column(name = "ship_city", nullable = false)
    private String shipCity;

    @Column(name = "ship_state", nullable = false)
    private String shipState;

    @Column(name = "ship_pincode", nullable = false)
    private String shipPincode;

    @Column(name = "razorpay_order_id")
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id")
    private String razorpayPaymentId;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "refund_id")
    private String refundId;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_status")
    private RefundStatus refundStatus;

    @Column(name = "refund_amount")
    private Long refundAmount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items = new ArrayList<>();

    public void addItem(OrderItem item) {
        item.setOrder(this);
        items.add(item);
    }
}