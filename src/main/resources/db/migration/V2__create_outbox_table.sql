-- V2: Outbox pattern table for reliable event publishing

CREATE TABLE outbox_events (
    id             UUID        PRIMARY KEY,
    event_type     VARCHAR(100) NOT NULL,
    payload        TEXT         NOT NULL,
    occurred_at    TIMESTAMPTZ  NOT NULL,
    correlation_id VARCHAR(100),
    published      BOOLEAN      NOT NULL DEFAULT FALSE,
    published_at   TIMESTAMPTZ
);

CREATE INDEX idx_outbox_unpublished ON outbox_events (published, occurred_at)
    WHERE published = FALSE;
