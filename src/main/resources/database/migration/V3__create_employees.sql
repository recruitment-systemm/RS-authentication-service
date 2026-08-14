CREATE TYPE employee_role_enum AS ENUM ( 'HR', 'INTERVIEWER');

CREATE TABLE employees (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    role employee_role_enum NOT NULL,
    ldap_uid VARCHAR(255) NOT NULL,
    organization_id UUID NOT NULL,

    CONSTRAINT fk_employee_organization
        FOREIGN KEY (organization_id)
            REFERENCES organizations(id)
            ON DELETE CASCADE,

    CONSTRAINT uk_employee_organization_username
        UNIQUE (organization_id, username),

    CONSTRAINT uk_employee_organization_ldap_uid
        UNIQUE (organization_id, ldap_uid)

);