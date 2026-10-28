-- Reviews, newsletter subscribers, contact messages and the activitylog activity log.

CREATE TABLE reviews
(
    id         UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    product_id UUID        NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    user_id    UUID        NOT NULL REFERENCES users (id),
    rating     INT         NOT NULL,
    comment    VARCHAR(1000),
    visible    BOOLEAN     NOT NULL DEFAULT TRUE, -- admins switch this off to hide a review
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_reviews_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT uq_reviews_user_product UNIQUE (user_id, product_id)
);

CREATE INDEX idx_reviews_product ON reviews (product_id, visible, created_at DESC);

CREATE TABLE newsletter_subscribers
(
    id            UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    subscribed_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX uq_newsletter_email ON newsletter_subscribers (LOWER(email));

CREATE TABLE contact_messages
(
    id         UUID PRIMARY KEY       DEFAULT gen_random_uuid(),
    name       VARCHAR(150)  NOT NULL,
    email      VARCHAR(255)  NOT NULL,
    subject    VARCHAR(200)  NOT NULL,
    message    VARCHAR(2000) NOT NULL,
    status     VARCHAR(10)   NOT NULL DEFAULT 'NEW',
    created_at TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_contact_messages_status CHECK (status IN ('NEW', 'READ', 'RESOLVED'))
);

CREATE INDEX idx_contact_messages_status ON contact_messages (status, created_at DESC);

CREATE TABLE activity_logs
(
    id          UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    admin_id    UUID         NOT NULL REFERENCES users (id),
    action      VARCHAR(200) NOT NULL, -- e.g. POST /api/activitylog/returns/{id}/approve
    entity_type VARCHAR(50),           -- e.g. returns, products, coupons
    entity_id   UUID,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_activity_logs_created ON activity_logs (created_at DESC);
CREATE INDEX idx_activity_logs_entity ON activity_logs (entity_type, created_at DESC);