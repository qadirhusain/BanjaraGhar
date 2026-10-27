ALTER TABLE orders
    ADD COLUMN shipped_at TIMESTAMPTZ,
    ADD COLUMN delivered_at TIMESTAMPTZ;

CREATE TABLE shipments
(
    id              UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    order_id        UUID        NOT NULL UNIQUE REFERENCES orders (id) ON DELETE CASCADE,
    status          VARCHAR(20) NOT NULL,
    sr_order_id     BIGINT,
    sr_shipment_id  BIGINT,
    awb_code        VARCHAR(50),
    courier_name    VARCHAR(100),
    tracking_status VARCHAR(100),
    attempts        INT         NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_error      VARCHAR(500),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_shipments_status CHECK (status IN ('PENDING', 'BOOKED', 'FAILED', 'CANCELLED'))
);

CREATE INDEX idx_shipments_due ON shipments (next_attempt_at) WHERE status = 'PENDING';
CREATE INDEX idx_shipments_awb ON shipments (awb_code);

CREATE TABLE return_requests
(
    id             UUID PRIMARY KEY       DEFAULT gen_random_uuid(),
    order_id       UUID          NOT NULL UNIQUE REFERENCES orders (id) ON DELETE CASCADE,
    user_id        UUID          NOT NULL REFERENCES users (id),
    status         VARCHAR(20)   NOT NULL,
    reason         VARCHAR(1000) NOT NULL,
    admin_note     VARCHAR(500),
    refund_amount  BIGINT,
    sr_order_id    BIGINT,
    sr_shipment_id BIGINT,
    return_awb     VARCHAR(50),
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    decided_at     TIMESTAMPTZ,
    CONSTRAINT chk_return_requests_status CHECK (status IN ('REQUESTED', 'APPROVED', 'REJECTED'))
);

CREATE INDEX idx_return_requests_status ON return_requests (status, created_at DESC);

CREATE TABLE return_photos
(
    id                UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    return_request_id UUID         NOT NULL REFERENCES return_requests (id) ON DELETE CASCADE,
    url               VARCHAR(500) NOT NULL,
    sort_order        INT          NOT NULL DEFAULT 0
);

CREATE INDEX idx_return_photos_request ON return_photos (return_request_id);