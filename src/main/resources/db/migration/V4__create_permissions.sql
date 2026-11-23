-- =====================================================
-- V4: Permisos por rol
-- Lo que puede hacer cada rol dentro de una empresa vive
-- en datos: se cambia con INSERT/DELETE en role_permissions.
-- =====================================================

-- Un rol privilegiado solo puede asignarse, cambiarse o retirarse
-- por quien tenga el permiso MEMBERS_MANAGE_PRIVILEGED.
ALTER TABLE roles
ADD COLUMN is_privileged BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE roles SET is_privileged = TRUE WHERE name = 'OWNER';


CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO permissions (code, description) VALUES
    ('MEMBERS_VIEW', 'Consultar los integrantes de la empresa'),
    ('MEMBERS_ADD', 'Agregar integrantes a la empresa'),
    ('MEMBERS_CHANGE_ROLE', 'Cambiar el rol de un integrante'),
    ('MEMBERS_REMOVE', 'Retirar integrantes de la empresa'),
    ('MEMBERS_MANAGE_PRIVILEGED', 'Gestionar integrantes con roles privilegiados');


CREATE TABLE role_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,

    CONSTRAINT role_permissions_pk
        PRIMARY KEY (role_id, permission_id),

    CONSTRAINT role_permissions_role_fk
        FOREIGN KEY (role_id)
        REFERENCES roles(id)
        ON DELETE CASCADE,

    CONSTRAINT role_permissions_permission_fk
        FOREIGN KEY (permission_id)
        REFERENCES permissions(id)
        ON DELETE CASCADE
);

CREATE INDEX role_permissions_permission_idx
ON role_permissions (permission_id);

-- ADMIN gestiona los integrantes de su empresa, sin tocar roles privilegiados.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'MEMBERS_VIEW',
    'MEMBERS_ADD',
    'MEMBERS_CHANGE_ROLE',
    'MEMBERS_REMOVE'
)
WHERE r.name = 'ADMIN';
