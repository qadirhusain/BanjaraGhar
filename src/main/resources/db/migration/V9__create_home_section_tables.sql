-- Product sections on the home page, chosen by the admins.

CREATE TABLE home_sections
(
    id         UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    title      VARCHAR(100) NOT NULL,
    subtitle   VARCHAR(250),
    layout     VARCHAR(10)  NOT NULL, -- GRID or SLIDER
    sort_order INT          NOT NULL DEFAULT 0,
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_home_sections_layout CHECK (layout IN ('GRID', 'SLIDER'))
);

CREATE TABLE home_section_products
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    section_id UUID NOT NULL REFERENCES home_sections (id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    sort_order INT  NOT NULL    DEFAULT 0,
    CONSTRAINT uq_home_section_products UNIQUE (section_id, product_id)
);

CREATE INDEX idx_home_sections_active ON home_sections (active, sort_order);
CREATE INDEX idx_home_section_products_section ON home_section_products (section_id, sort_order);