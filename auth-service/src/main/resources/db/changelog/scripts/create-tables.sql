--liquibase formatted sql
--changeset Pavlochev.SY:create_tables

DROP TABLE IF EXISTS app_users CASCADE;
DROP TABLE IF EXISTS outbox_events CASCADE;

CREATE TABLE app_users
(
    id            UUID                        NOT NULL,
    email         VARCHAR(255)                NOT NULL,
    password_hash VARCHAR(255)                NOT NULL,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_app_users PRIMARY KEY (id)
);
ALTER TABLE app_users
    ADD CONSTRAINT uc_app_users_email UNIQUE (email);

CREATE TABLE outbox_events
(
    id             UUID                        NOT NULL,
    event_id       UUID                        NOT NULL,
    event_type     VARCHAR(255)                NOT NULL,
    aggregate_id   UUID                        NOT NULL,
    aggregate_type VARCHAR(255)                NOT NULL,
    payload        JSONB                       NOT NULL,
    created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    published_at   TIMESTAMP WITHOUT TIME ZONE,
    status         VARCHAR(255)                NOT NULL,
    CONSTRAINT pk_outbox_events PRIMARY KEY (id)
);
ALTER TABLE outbox_events
    ADD CONSTRAINT uc_outbox_events_eventid UNIQUE (event_id);

