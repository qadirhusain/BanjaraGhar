-- Banners for the home page and for category pages.

CREATE TABLE banners
(
    id               UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    title            VARCHAR(150),
    subtitle         VARCHAR(250),
    button_text      VARCHAR(40),
    link_url         VARCHAR(500),                                      -- where the banner leads, e.g. /categories/home-decor
    image_url        VARCHAR(500) NOT NULL,
    mobile_image_url VARCHAR(500),                                      -- optional tall version for phones and the iOS app
    placement        VARCHAR(10)  NOT NULL,                             -- HOME or CATEGORY
    category_id      UUID REFERENCES categories (id) ON DELETE CASCADE, -- only for CATEGORY banners
    sort_order       INT          NOT NULL DEFAULT 0,
    active           BOOLEAN      NOT NULL DEFAULT TRUE,
    starts_at        TIMESTAMPTZ,                                       -- empty = shown immediately
    ends_at          TIMESTAMPTZ,                                       -- empty = shown until switched off
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_banners_placement CHECK (placement IN ('HOME', 'CATEGORY')),
    CONSTRAINT chk_banners_category CHECK (
        (placement = 'CATEGORY' AND category_id IS NOT NULL) OR (placement = 'HOME' AND category_id IS NULL)),
    CONSTRAINT chk_banners_dates CHECK (starts_at IS NULL OR ends_at IS NULL OR starts_at < ends_at)
);

CREATE INDEX idx_banners_placement ON banners (placement, active, sort_order);
CREATE INDEX idx_banners_category_id ON banners (category_id);