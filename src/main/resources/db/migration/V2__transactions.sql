CREATE TABLE tx_sch.transactions
(
    id                      UUID           NOT NULL,
    reference               VARCHAR(32)    NOT NULL,
    type                    VARCHAR(16)    NOT NULL,
    status                  VARCHAR(16)    NOT NULL,
    amount                  NUMERIC(19, 4) NOT NULL,
    currency                VARCHAR(3)     NOT NULL,
    source_account_id       BIGINT,
    target_account_id       BIGINT,
    source_user_id          BIGINT,
    target_user_id          BIGINT,
    idempotency_key         VARCHAR(64)    NOT NULL,
    original_transaction_id UUID,
    description             VARCHAR(255),
    failure_reason          VARCHAR(512),
    initiated_at            TIMESTAMPTZ    NOT NULL,
    completed_at            TIMESTAMPTZ,
    device_id               VARCHAR(128),
    ip_address              VARCHAR(45),

    is_deleted              BOOLEAN        NOT NULL DEFAULT FALSE,
    version                 BIGINT         NOT NULL DEFAULT 0,
    created_at              TIMESTAMPTZ,
    created_by              VARCHAR(64),
    updated_at              TIMESTAMPTZ,
    updated_by              VARCHAR(64),

    CONSTRAINT pk_transactions PRIMARY KEY (id),
    CONSTRAINT uq_tx_reference UNIQUE (reference),
    CONSTRAINT uq_tx_user_idempotency UNIQUE (source_user_id, idempotency_key),

    -- Manfiy yoki nol summa DB darajasida bloklanadi.
    -- Application bug'i bo'lsa ham, ledger buzilmaydi.
    CONSTRAINT ck_tx_amount_positive CHECK (amount > 0),
    CONSTRAINT ck_tx_type CHECK (type IN ('TRANSFER', 'DEPOSIT', 'WITHDRAW', 'REFUND')),
    CONSTRAINT ck_tx_status CHECK (status IN
                                   ('INITIATED', 'PENDING', 'COMPLETED', 'FAILED', 'CANCELLED', 'REFUNDED')),
    CONSTRAINT ck_tx_currency CHECK (currency IN ('UZS', 'USD', 'EUR', 'RUB'))
);

COMMENT ON TABLE tx_sch.transactions IS 'Biznes fakti. Buxgalteriya yozuvlari transaction_entries jadvalida.';
