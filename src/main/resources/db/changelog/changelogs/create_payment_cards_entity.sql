-- liquibase formatted sql

--changeset Maltsev:create-payment_cards_table
CREATE TABLE payment_cards
(
    id              SERIAL PRIMARY KEY,
    user_id         INTEGER     NOT NULL,
    number          VARCHAR(50) NOT NULL UNIQUE,
    expiration_date TIMESTAMP   NOT NULL,
    holder          VARCHAR(150),
    active          BOOLEAN     NOT NULL,
    created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT users_payment_cards FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);
--rollback DROP TABLE payment_cards

--changeset Maltsev:add-idx-to-payment_cards
CREATE INDEX idx_payment_cards_user_id ON payment_cards (user_id);
-- rollback DROP INDEX idx_payment_cards_user_id;