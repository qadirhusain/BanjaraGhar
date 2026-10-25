CREATE TABLE coupons
(
    id                  UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    code                VARCHAR(30) NOT NULL,           -- always stored in upper case
    description         VARCHAR(200),
    discount_type       VARCHAR(10) NOT NULL,
    discount_value      BIGINT      NOT NULL,           -- percent (1-100) or paise, depending on the type
    max_discount_amount BIGINT,                         -- paise cap, only for PERCENT coupons
    min_order_amount    BIGINT      NOT NULL DEFAULT 0, -- paise
    usage_limit         INT,                            -- total uses across all customers, empty = unlimited
    per_user_limit      INT         NOT NULL DEFAULT 1,
    starts_at           TIMESTAMPTZ,                    -- empty = valid immediately
    expires_at          TIMESTAMPTZ,                    -- empty = never expires
    active              BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_coupons_type CHECK (discount_type IN ('PERCENT', 'FIXED')),
    CONSTRAINT chk_coupons_value CHECK (discount_value > 0),
    CONSTRAINT chk_coupons_percent CHECK (discount_type <> 'PERCENT' OR discount_value <= 100),
    CONSTRAINT chk_coupons_max_discount CHECK (max_discount_amount IS NULL OR max_discount_amount > 0),
    CONSTRAINT chk_coupons_min_order CHECK (min_order_amount >= 0),
    CONSTRAINT chk_coupons_usage_limit CHECK (usage_limit IS NULL OR usage_limit > 0),
    CONSTRAINT chk_coupons_per_user_limit CHECK (per_user_limit > 0),
    CONSTRAINT chk_coupons_dates CHECK (starts_at IS NULL OR expires_at IS NULL OR starts_at < expires_at)
);

CREATE UNIQUE INDEX uq_coupons_code ON coupons (UPPER(code));

-- One row per order that used a coupon. Checkout (Step 6b) adds the row and the foreign key to orders;
-- cancelling or expiring an unpaid order removes it, which frees the use again.
CREATE TABLE coupon_redemptions
(
    id         UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    coupon_id  UUID        NOT NULL REFERENCES coupons (id),
    user_id    UUID        NOT NULL REFERENCES users (id),
    order_id   UUID        NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_coupon_redemptions_coupon_user ON coupon_redemptions (coupon_id, user_id);