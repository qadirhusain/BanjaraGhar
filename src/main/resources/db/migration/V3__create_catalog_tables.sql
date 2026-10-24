-- Catalog tables. All money columns are in paise.

CREATE TABLE categories
(
    id          UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    slug        VARCHAR(120) NOT NULL UNIQUE,
    description VARCHAR(500),
    image       VARCHAR(500),
    sort_order  INT          NOT NULL DEFAULT 0,
    active      BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE UNIQUE INDEX uq_categories_name ON categories (LOWER(name));

CREATE TABLE products
(
    id                UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    category_id       UUID         NOT NULL REFERENCES categories (id),
    name              VARCHAR(200) NOT NULL,
    slug              VARCHAR(220) NOT NULL UNIQUE,
    description       TEXT         NOT NULL,
    price             BIGINT       NOT NULL,
    mrp               BIGINT       NOT NULL,
    material          VARCHAR(150),
    colour            VARCHAR(150),
    size              VARCHAR(100),
    care_instructions VARCHAR(255),
    stock_quantity    INT          NOT NULL DEFAULT 0,
    featured          BOOLEAN      NOT NULL DEFAULT FALSE,
    sku               VARCHAR(50)  NOT NULL,
    packed_weight_g   INT          NOT NULL,
    packed_length_cm  INT          NOT NULL,
    packed_breadth_cm INT          NOT NULL,
    packed_height_cm  INT          NOT NULL,
    active            BOOLEAN      NOT NULL DEFAULT FALSE, -- new products stay hidden until published
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    -- Used by PostgreSQL text search; maintained by the database itself
    search_vector     TSVECTOR GENERATED ALWAYS AS (
        to_tsvector('simple',
                    name || ' ' || description || ' ' || COALESCE(material, '') || ' ' || COALESCE(colour, ''))
        ) STORED,
    CONSTRAINT chk_products_price CHECK (price > 0),
    CONSTRAINT chk_products_mrp CHECK (mrp >= price),
    CONSTRAINT chk_products_stock CHECK (stock_quantity >= 0)
);

CREATE UNIQUE INDEX uq_products_sku ON products (LOWER(sku));
CREATE INDEX idx_products_category_id ON products (category_id);
CREATE INDEX idx_products_search ON products USING GIN (search_vector);

CREATE TABLE product_images
(
    id         UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    product_id UUID         NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    url        VARCHAR(500) NOT NULL,
    sort_order INT          NOT NULL DEFAULT 0
);

CREATE INDEX idx_product_images_product_id ON product_images (product_id, sort_order);

CREATE TABLE related_products
(
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id         UUID NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    related_product_id UUID NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    sort_order         INT  NOT NULL    DEFAULT 0,
    CONSTRAINT uq_related_products_pair UNIQUE (product_id, related_product_id),
    CONSTRAINT chk_related_products_not_self CHECK (product_id <> related_product_id)
);

CREATE INDEX idx_related_products_product_id ON related_products (product_id);