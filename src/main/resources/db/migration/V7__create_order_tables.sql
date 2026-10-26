-- Orders. All money columns are in paise.

CREATE SEQUENCE order_number_seq START WITH 1001;

CREATE TABLE orders
(
    id                  UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    order_number        VARCHAR(20)  NOT NULL UNIQUE, -- shown to the customer, e.g. BG1001
    user_id             UUID         NOT NULL REFERENCES users (id),
    status              VARCHAR(20)  NOT NULL,
    subtotal            BIGINT       NOT NULL,
    discount_amount     BIGINT       NOT NULL DEFAULT 0,
    shipping_fee        BIGINT       NOT NULL DEFAULT 0,
    total_amount        BIGINT       NOT NULL,
    coupon_code         VARCHAR(30),
    -- Shipping address copied at checkout, so later edits to the saved address never change an order
    ship_name           VARCHAR(150) NOT NULL,
    ship_phone          VARCHAR(20)  NOT NULL,
    ship_line1          VARCHAR(255) NOT NULL,
    ship_line2          VARCHAR(255),
    ship_city           VARCHAR(100) NOT NULL,
    ship_state          VARCHAR(100) NOT NULL,
    ship_pincode        VARCHAR(10)  NOT NULL,
    razorpay_order_id   VARCHAR(50) UNIQUE,
    razorpay_payment_id VARCHAR(50),
    paid_at             TIMESTAMPTZ,
    expires_at          TIMESTAMPTZ  NOT NULL,        -- unpaid orders release their stock after this
    cancelled_at        TIMESTAMPTZ,
    refund_id           VARCHAR(50),
    refund_status       VARCHAR(20),
    refund_amount       BIGINT,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_orders_status CHECK (status IN
                                        ('PENDING_PAYMENT', 'PAID', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'EXPIRED')),
    CONSTRAINT chk_orders_refund_status CHECK (refund_status IS NULL OR
                                               refund_status IN ('INITIATED', 'PROCESSED', 'FAILED')),
    CONSTRAINT chk_orders_total CHECK (total_amount > 0)
);

CREATE INDEX idx_orders_user_created ON orders (user_id, created_at DESC);
CREATE INDEX idx_orders_pending_expiry ON orders (expires_at) WHERE status = 'PENDING_PAYMENT';

CREATE TABLE order_items
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id     UUID         NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id   UUID         NOT NULL REFERENCES products (id),
    -- Product details copied at checkout, so later catalog edits never change an order
    product_name VARCHAR(200) NOT NULL,
    sku          VARCHAR(50)  NOT NULL,
    image_url    VARCHAR(500),
    unit_price   BIGINT       NOT NULL,
    quantity     INT          NOT NULL,
    line_total   BIGINT       NOT NULL,
    CONSTRAINT chk_order_items_quantity CHECK (quantity > 0)
);

CREATE INDEX idx_order_items_order_id ON order_items (order_id);
CREATE INDEX idx_order_items_product_id ON order_items (product_id);

-- Now that orders exist, tie coupon uses to them
ALTER TABLE coupon_redemptions
    ADD CONSTRAINT fk_coupon_redemptions_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE;