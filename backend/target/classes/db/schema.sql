-- FlexiSlot SaaS - PostgreSQL Schema
-- All tables use ULID VARCHAR(26) as primary key

-- ----------------------------
-- Table: users
-- ----------------------------
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(26) NOT NULL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('ADMIN','BUSINESS_OWNER','CUSTOMER')),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- ----------------------------
-- Table: business
-- ----------------------------
CREATE TABLE IF NOT EXISTS business (
    id VARCHAR(26) NOT NULL PRIMARY KEY,
    owner_user_id VARCHAR(26) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    location VARCHAR(500),
    service_type VARCHAR(100),
    operating_hours TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_business_owner FOREIGN KEY (owner_user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_business_owner ON business(owner_user_id);

-- ----------------------------
-- Table: customer
-- ----------------------------
CREATE TABLE IF NOT EXISTS customer (
    id VARCHAR(26) NOT NULL PRIMARY KEY,
    user_id VARCHAR(26) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_customer_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_customer_user ON customer(user_id);

-- ----------------------------
-- Table: services
-- ----------------------------
CREATE TABLE IF NOT EXISTS services (
    id VARCHAR(26) NOT NULL PRIMARY KEY,
    business_id VARCHAR(26) NOT NULL,
    service_name VARCHAR(255) NOT NULL,
    base_price DECIMAL(12,2) NOT NULL,
    duration_minutes INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_services_business FOREIGN KEY (business_id) REFERENCES business(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_services_business ON services(business_id);

-- ----------------------------
-- Table: slot
-- ----------------------------
CREATE TABLE IF NOT EXISTS slot (
    id VARCHAR(26) NOT NULL PRIMARY KEY,
    service_id VARCHAR(26) NOT NULL,
    slot_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    status VARCHAR(50) NOT NULL CHECK (status IN ('AVAILABLE','BOOKED','CANCELLED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_slot_service FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_slot_service ON slot(service_id);
CREATE INDEX IF NOT EXISTS idx_slot_date ON slot(slot_date);
CREATE INDEX IF NOT EXISTS idx_slot_service_date ON slot(service_id, slot_date);
CREATE INDEX IF NOT EXISTS idx_slot_status ON slot(status);

-- ----------------------------
-- Table: bookings
-- ----------------------------
CREATE TABLE IF NOT EXISTS bookings (
    id VARCHAR(26) NOT NULL PRIMARY KEY,
    customer_id VARCHAR(26) NOT NULL,
    slot_id VARCHAR(26) NOT NULL UNIQUE,
    booking_status VARCHAR(50) NOT NULL CHECK (booking_status IN ('CONFIRMED','CANCELLED','COMPLETED')),
    payment_status VARCHAR(50) NOT NULL CHECK (payment_status IN ('INITIATED','SUCCESS','FAILED')),
    booking_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bookings_customer FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE,
    CONSTRAINT fk_bookings_slot FOREIGN KEY (slot_id) REFERENCES slot(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_bookings_customer ON bookings(customer_id);
CREATE INDEX IF NOT EXISTS idx_bookings_slot ON bookings(slot_id);
CREATE INDEX IF NOT EXISTS idx_bookings_customer_status ON bookings(customer_id, booking_status);

-- ----------------------------
-- Table: payments
-- ----------------------------
CREATE TABLE IF NOT EXISTS payments (
    id VARCHAR(26) NOT NULL PRIMARY KEY,
    booking_id VARCHAR(26) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    provider VARCHAR(100) NOT NULL,
    provider_reference VARCHAR(255),
    status VARCHAR(50) NOT NULL CHECK (status IN ('INITIATED','SUCCESS','FAILED')),
    paid_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payments_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_payments_booking ON payments(booking_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
