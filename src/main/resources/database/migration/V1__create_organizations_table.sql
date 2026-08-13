CREATE TYPE organization_status AS ENUM (
    'PENDING',
    'ACCEPTED',
    'REJECTED'
);
CREATE TABLE organizations (
id UUID PRIMARY KEY,
name VARCHAR(255) NOT NULL,
email VARCHAR(255) NOT NULL UNIQUE,
password_hash VARCHAR(255) NOT NULL,
status organization_status NOT NULL DEFAULT 'PENDING',
requested_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
tax_registration_number VARCHAR(16) NOT NULL UNIQUE CHECK (tax_registration_number ~ '^EG-[0-9]{3}-[0-9]{3}-[0-9]{3}$'),
tax_registration_document VARCHAR(500) NOT NULL
);
CREATE INDEX idx_organizations_status ON organizations(status);