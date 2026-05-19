-- liquibase formatted sql

--changeset Maltsev:create-users_table
CREATE TABLE users
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    surname    VARCHAR(100) NOT NULL,
    birth_date DATE         NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    active     BOOLEAN      NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
--rollback DROP TABLE users
