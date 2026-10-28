-- Shipping fee rules that admins can change without redeploying. Always exactly one row.

CREATE TABLE shipping_settings
(
    id         UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    singleton  BOOLEAN     NOT NULL DEFAULT TRUE UNIQUE, -- UNIQUE + CHECK below guarantee only one row can exist
    flat_fee   BIGINT      NOT NULL,                     -- paise
    free_above BIGINT      NOT NULL,                     -- paise, orders at or above this ship free
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_shipping_settings_singleton CHECK (singleton),
    CONSTRAINT chk_shipping_settings_fee CHECK (flat_fee >= 0),
    CONSTRAINT chk_shipping_settings_free_above CHECK (free_above >= 0)
);

INSERT INTO shipping_settings (flat_fee, free_above)
VALUES (8900, 99900); -- Rs 89 fee, free from Rs 999