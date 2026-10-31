
-- =====================================================
-- V1: Initial database schema
-- Parking Management SaaS
-- =====================================================

-- Extensión para restricciones de exclusión con UUID
CREATE EXTENSION IF NOT EXISTS btree_gist;


-- =====================================================
-- 1. USERS
-- Usuarios registrados en la plataforma
-- =====================================================

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_type VARCHAR(20) NOT NULL DEFAULT 'DNI',
    document_number VARCHAR(20) NOT NULL,

    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(30),

    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT users_document_type_check
        CHECK (document_type IN ('DNI', 'CE', 'PASSPORT'))
);

CREATE UNIQUE INDEX users_email_unique
ON users (LOWER(email));

CREATE UNIQUE INDEX users_document_unique
ON users (document_type, document_number);

-- =====================================================
-- 2. COMPANIES
-- Empresas que utilizan el SaaS
-- =====================================================

CREATE TABLE companies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_name VARCHAR(200) NOT NULL,
    tax_id VARCHAR(30),
    email VARCHAR(255),
    phone VARCHAR(30),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT companies_status_check
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'))
);

CREATE UNIQUE INDEX companies_tax_id_unique
ON companies (tax_id)
WHERE tax_id IS NOT NULL;


-- =====================================================
-- 3. ROLES
-- Roles disponibles en una empresa
-- =====================================================

CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO roles (name, description) VALUES
    ('OWNER', 'Propietario de la empresa'),
    ('ADMIN', 'Administrador de la empresa'),
    ('EMPLOYEE', 'Empleado de la empresa');


-- =====================================================
-- 4. COMPANY MEMBERS
-- Usuarios asociados a empresas
-- =====================================================

CREATE TABLE company_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT company_members_company_fk
        FOREIGN KEY (company_id)
        REFERENCES companies(id),

    CONSTRAINT company_members_user_fk
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT company_members_role_fk
        FOREIGN KEY (role_id)
        REFERENCES roles(id),

    CONSTRAINT company_members_unique
        UNIQUE (company_id, user_id)
);

CREATE INDEX company_members_user_idx
ON company_members (user_id);


-- =====================================================
-- 5. PARKINGS
-- Estacionamientos o sedes de cada empresa
-- =====================================================

CREATE TABLE parkings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    address VARCHAR(255) NOT NULL,
    latitude NUMERIC(10, 7),
    longitude NUMERIC(10, 7),
    phone VARCHAR(30),
    timezone VARCHAR(50) NOT NULL DEFAULT 'America/Lima',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT parkings_company_fk
        FOREIGN KEY (company_id)
        REFERENCES companies(id),

    CONSTRAINT parkings_status_check
        CHECK (status IN ('ACTIVE', 'INACTIVE')),

    CONSTRAINT parkings_latitude_check
        CHECK (latitude IS NULL OR latitude BETWEEN -90 AND 90),

    CONSTRAINT parkings_longitude_check
        CHECK (longitude IS NULL OR longitude BETWEEN -180 AND 180)
);

CREATE INDEX parkings_company_idx
ON parkings (company_id);


-- =====================================================
-- 6. PARKING SPACES
-- Espacios de estacionamiento
-- =====================================================

CREATE TABLE parking_spaces (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parking_id UUID NOT NULL,
    code VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    space_type VARCHAR(30) NOT NULL,
    hourly_rate NUMERIC(10, 2),
    daily_rate NUMERIC(10, 2),
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT parking_spaces_parking_fk
        FOREIGN KEY (parking_id)
        REFERENCES parkings(id),

    CONSTRAINT parking_spaces_type_check
        CHECK (space_type IN ('CAR', 'MOTORCYCLE', 'SUV', 'BICYCLE')),

    CONSTRAINT parking_spaces_status_check
        CHECK (status IN ('AVAILABLE', 'MAINTENANCE', 'INACTIVE')),

    CONSTRAINT parking_spaces_rates_check
        CHECK (
            (hourly_rate IS NULL OR hourly_rate >= 0)
            AND
            (daily_rate IS NULL OR daily_rate >= 0)
            AND
            (hourly_rate IS NOT NULL OR daily_rate IS NOT NULL)
        ),

    CONSTRAINT parking_spaces_code_unique
        UNIQUE (parking_id, code)
);

CREATE INDEX parking_spaces_parking_idx
ON parking_spaces (parking_id);


-- =====================================================
-- 7. PARKING SCHEDULES
-- Horarios de atención por día de la semana
-- 1 = Monday, 7 = Sunday
-- =====================================================

CREATE TABLE parking_schedules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parking_id UUID NOT NULL,
    day_of_week SMALLINT NOT NULL,
    opening_time TIME,
    closing_time TIME,
    closes_next_day BOOLEAN NOT NULL DEFAULT FALSE,
    is_closed BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT parking_schedules_parking_fk
        FOREIGN KEY (parking_id)
        REFERENCES parkings(id),

    CONSTRAINT parking_schedules_day_check
        CHECK (day_of_week BETWEEN 1 AND 7),

    CONSTRAINT parking_schedules_unique
        UNIQUE (parking_id, day_of_week),

    CONSTRAINT parking_schedules_hours_check
        CHECK (
            (
                is_closed = TRUE
                AND opening_time IS NULL
                AND closing_time IS NULL
                AND closes_next_day = FALSE
            )
            OR
            (
                is_closed = FALSE
                AND opening_time IS NOT NULL
                AND closing_time IS NOT NULL
                AND opening_time <> closing_time
            )
        )
);


-- =====================================================
-- 8. VEHICLES
-- Vehículos de usuarios registrados
-- =====================================================

CREATE TABLE vehicles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    license_plate VARCHAR(20) NOT NULL,
    vehicle_type VARCHAR(30) NOT NULL,
    brand VARCHAR(100),
    model VARCHAR(100),
    color VARCHAR(50),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT vehicles_user_fk
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT vehicles_type_check
        CHECK (vehicle_type IN ('CAR', 'MOTORCYCLE', 'SUV', 'BICYCLE'))
);

CREATE UNIQUE INDEX vehicles_license_plate_unique
ON vehicles (LOWER(license_plate));

CREATE INDEX vehicles_user_idx
ON vehicles (user_id);


-- =====================================================
-- 9. RESERVATIONS
-- Reservas por hora o por día
-- =====================================================

CREATE TABLE reservations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parking_space_id UUID NOT NULL,
    user_id UUID,
    vehicle_id UUID,

    -- Datos del cliente invitado
    guest_name VARCHAR(150),
    guest_email VARCHAR(255),
    guest_phone VARCHAR(30),

    -- Datos históricos del vehículo
    license_plate VARCHAR(20) NOT NULL,
    vehicle_type VARCHAR(30) NOT NULL,

    reservation_type VARCHAR(20) NOT NULL,
    start_at TIMESTAMPTZ NOT NULL,
    end_at TIMESTAMPTZ NOT NULL,

    -- Tarifas aplicadas al momento de reservar
    hourly_rate_snapshot NUMERIC(10, 2),
    daily_rate_snapshot NUMERIC(10, 2),
    total_amount NUMERIC(10, 2) NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT reservations_parking_space_fk
        FOREIGN KEY (parking_space_id)
        REFERENCES parking_spaces(id),

    CONSTRAINT reservations_user_fk
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT reservations_vehicle_fk
        FOREIGN KEY (vehicle_id)
        REFERENCES vehicles(id),

    CONSTRAINT reservations_valid_dates
        CHECK (end_at > start_at),

    CONSTRAINT reservations_valid_amount
        CHECK (total_amount >= 0),

    CONSTRAINT reservations_vehicle_type_check
        CHECK (vehicle_type IN ('CAR', 'MOTORCYCLE', 'SUV', 'BICYCLE')),

    CONSTRAINT reservations_type_check
        CHECK (reservation_type IN ('HOURLY', 'DAILY')),

    CONSTRAINT reservations_status_check
        CHECK (
            status IN (
                'PENDING',
                'CONFIRMED',
                'CANCELLED',
                'COMPLETED',
                'NO_SHOW'
            )
        ),

    CONSTRAINT reservations_client_check
        CHECK (
            (
                user_id IS NOT NULL
                AND guest_name IS NULL
                AND guest_email IS NULL
                AND guest_phone IS NULL
            )
            OR
            (
                user_id IS NULL
                AND guest_name IS NOT NULL
                AND guest_phone IS NOT NULL
            )
        ),

    CONSTRAINT reservations_rate_check
        CHECK (
            (
                reservation_type = 'HOURLY'
                AND hourly_rate_snapshot IS NOT NULL
                AND hourly_rate_snapshot >= 0
            )
            OR
            (
                reservation_type = 'DAILY'
                AND daily_rate_snapshot IS NOT NULL
                AND daily_rate_snapshot >= 0
            )
        )
);

CREATE INDEX reservations_parking_space_idx
ON reservations (parking_space_id);

CREATE INDEX reservations_user_idx
ON reservations (user_id);

CREATE INDEX reservations_dates_idx
ON reservations (start_at, end_at);

CREATE INDEX reservations_status_idx
ON reservations (status);


-- =====================================================
-- PREVENT OVERLAPPING ACTIVE RESERVATIONS
-- [) permite que una reserva comience exactamente
-- cuando termina otra.
-- =====================================================

ALTER TABLE reservations
ADD CONSTRAINT reservations_no_overlapping_active
EXCLUDE USING GIST (
    parking_space_id WITH =,
    tstzrange(start_at, end_at, '[)') WITH &&
)
WHERE (status IN ('PENDING', 'CONFIRMED'));
