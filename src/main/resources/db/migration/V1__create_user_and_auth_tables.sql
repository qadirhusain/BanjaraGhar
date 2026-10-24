-- User and Auth tables

CREATE TABLE users (
   id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
   email          VARCHAR(255) NOT NULL,
   password_hash  VARCHAR(255),                       -- empty for social-only accounts
   full_name      VARCHAR(150) NOT NULL,
   phone          VARCHAR(20),
   role           VARCHAR(20)  NOT NULL DEFAULT 'CUSTOMER',
   email_verified BOOLEAN      NOT NULL DEFAULT FALSE,
   created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
   deleted_at     TIMESTAMPTZ,                        -- set when the customer deletes the account
   CONSTRAINT chk_users_role CHECK (role IN ('CUSTOMER', 'ADMIN'))
);

-- One active account per email; a deleted account frees its email for re-registration
CREATE UNIQUE INDEX uq_users_email_active ON users (LOWER(email)) WHERE deleted_at IS NULL;

CREATE TABLE addresses (
   id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
   user_id    UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
   name       VARCHAR(150) NOT NULL,
   phone      VARCHAR(20)  NOT NULL,
   line1      VARCHAR(255) NOT NULL,
   line2      VARCHAR(255),
   city       VARCHAR(100) NOT NULL,
   state      VARCHAR(100) NOT NULL,
   pincode    VARCHAR(10)  NOT NULL,
   is_default BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_addresses_user_id ON addresses (user_id);

-- At most one default address per user
CREATE UNIQUE INDEX uq_addresses_default_per_user ON addresses (user_id) WHERE is_default;

CREATE TABLE auth_identities (
     id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
     user_id          UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
     provider         VARCHAR(20)  NOT NULL,
     provider_user_id VARCHAR(255) NOT NULL,
     created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
     CONSTRAINT chk_auth_identities_provider CHECK (provider IN ('GOOGLE', 'FACEBOOK', 'APPLE')),
     CONSTRAINT uq_auth_identities_provider_user UNIQUE (provider, provider_user_id)
);

CREATE INDEX idx_auth_identities_user_id ON auth_identities (user_id);

CREATE TABLE email_otps (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email      VARCHAR(255) NOT NULL,
    purpose    VARCHAR(30)  NOT NULL,
    hashed_otp VARCHAR(255) NOT NULL,
    attempts   INT          NOT NULL DEFAULT 0,
    expires_at TIMESTAMPTZ  NOT NULL,
    used       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_email_otps_purpose CHECK (purpose IN ('SIGNUP', 'PASSWORD_RESET'))
);

CREATE INDEX idx_email_otps_email_purpose ON email_otps (email, purpose);

CREATE TABLE refresh_tokens (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    hashed_token VARCHAR(255) NOT NULL UNIQUE,
    expires_at   TIMESTAMPTZ  NOT NULL,
    revoked      BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);