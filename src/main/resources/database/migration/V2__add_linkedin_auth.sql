ALTER TABLE organizations ALTER COLUMN password_hash DROP NOT NULL;
ALTER TABLE organizations ADD COLUMN linkedin_id VARCHAR(255) UNIQUE;
CREATE TYPE provider_enum AS ENUM ('LOCAL','LINKEDIN');
ALTER TABLE organizations ADD COLUMN auth_provider provider_enum NOT NULL DEFAULT 'LOCAL';