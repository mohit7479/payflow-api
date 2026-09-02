DELETE FROM transactions;
ALTER TABLE transactions ADD COLUMN idempotency_key VARCHAR(255) NOT NULL UNIQUE;