-- =====================================================
-- V3: Owner de la plataforma
-- Marca al usuario que administra todas las empresas.
-- El índice único parcial garantiza que exista uno solo.
-- El flag no se asigna desde la API: se promueve a un
-- usuario ya registrado con un UPDATE manual.
-- =====================================================

ALTER TABLE users
ADD COLUMN is_platform_owner BOOLEAN NOT NULL DEFAULT FALSE;

CREATE UNIQUE INDEX users_single_platform_owner
ON users (is_platform_owner)
WHERE is_platform_owner;
