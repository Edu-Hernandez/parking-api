-- =====================================================
-- V5: Permisos de estacionamientos
-- Lo que puede hacer cada rol sobre los parkings de su
-- empresa vive en datos: se cambia con INSERT/DELETE en
-- role_permissions.
-- =====================================================

INSERT INTO permissions (code, description) VALUES
    ('PARKINGS_VIEW', 'Consultar los estacionamientos de la empresa'),
    ('PARKINGS_CREATE', 'Crear estacionamientos en la empresa'),
    ('PARKINGS_UPDATE', 'Editar los datos de un estacionamiento'),
    ('PARKINGS_CHANGE_STATUS', 'Activar y desactivar estacionamientos');

-- ADMIN gestiona todos los estacionamientos de su empresa.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'PARKINGS_VIEW',
    'PARKINGS_CREATE',
    'PARKINGS_UPDATE',
    'PARKINGS_CHANGE_STATUS'
)
WHERE r.name = 'ADMIN';

-- EMPLOYEE solo consulta. Para darle más, se agrega una fila en role_permissions.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code = 'PARKINGS_VIEW'
WHERE r.name = 'EMPLOYEE';
