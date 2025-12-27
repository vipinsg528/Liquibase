--liquibase formatted sql

-- changeset site_dev:1-create-employee-table
CREATE TABLE IF NOT EXISTS employee (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    department VARCHAR(100)
);

-- changeset site_dev:2-create-app-user-table
CREATE TABLE IF NOT EXISTS app_user (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        username VARCHAR(150) UNIQUE NOT NULL,
        password VARCHAR(255) NOT NULL,
        role     VARCHAR(50)  NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- changeset site_dev:3-add-index-on-username
CREATE INDEX IF NOT EXISTS idx_app_user_username
    ON app_user(username);

-- changeset site_dev:4-create-app-user-roles-table
CREATE TABLE IF NOT EXISTS app_user_roles (
                 user_id UUID NOT NULL,
                 role VARCHAR(50) NOT NULL,
                 PRIMARY KEY (user_id, role),
                 CONSTRAINT fk_app_user_roles_user
                 FOREIGN KEY (user_id)
                 REFERENCES app_user(id)
                 ON DELETE CASCADE
);

-- changeset site_dev:5-remove-role-column-from-app-user
ALTER TABLE app_user
    DROP COLUMN IF EXISTS role;