-- 1) Cursor pagination: GET /api/v1/transactions
--    WHERE (created_at, id) < (?, ?) ORDER BY created_at DESC, id DESC
--    Composite index aynan shu ORDER BY'ni index-only scan qiladi.
CREATE INDEX ix_tx_cursor ON tx_sch.transactions (created_at DESC, id DESC);

-- 2) "Mening tranzaksiyalarim" — user bo'yicha filtr + cursor
CREATE INDEX ix_tx_source_user_created ON tx_sch.transactions (source_user_id, created_at DESC);

-- 3) Hisob bo'yicha tarix (ikki tomonlama qidiruv)
CREATE INDEX ix_tx_source_account ON tx_sch.transactions (source_account_id);
CREATE INDEX ix_tx_target_account ON tx_sch.transactions (target_account_id);

-- 4) Outbox poller: SELECT ... WHERE status='NEW' ORDER BY created_at FOR UPDATE SKIP LOCKED
--    PARTIAL index: PUBLISHED satrlar (99.9%) indeksda umuman turmaydi.
--    Jadval millionlab satrga o'ssa ham, bu indeks kichik qoladi.
CREATE INDEX ix_outbox_pending ON tx_sch.outbox_events (created_at)
    WHERE status = 'NEW';

-- 5) StuckTransactionJob: uzoq PENDING'da qolganlarni topish
CREATE INDEX ix_tx_pending_initiated ON tx_sch.transactions (initiated_at)
    WHERE status = 'PENDING';

-- 6) Reconciliation: kunlik settled tranzaksiyalar
CREATE INDEX ix_tx_completed_at ON tx_sch.transactions (completed_at)
    WHERE status IN ('COMPLETED', 'REFUNDED');
