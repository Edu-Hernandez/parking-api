-- =====================================================
-- V2: Refresh tokens
-- Sesiones de larga duración que permiten renovar el
-- access token (JWT corto) y revocarlo en logout.
-- Solo se guarda el hash SHA-256 del token, nunca el valor.
-- =====================================================

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT refresh_tokens_user_fk
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT refresh_tokens_token_hash_unique
        UNIQUE (token_hash)
);

CREATE INDEX refresh_tokens_user_active_idx
ON refresh_tokens (user_id)
WHERE revoked_at IS NULL;
