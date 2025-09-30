-- Enable UUID generation if not already enabled in the database
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Create the new table for tracking email verification batches
CREATE TABLE xtremand_production.xt_email_verification_batch (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_emails INT NOT NULL,
    valid_emails INT DEFAULT 0,
    invalid_emails INT DEFAULT 0,
    deliverable_emails INT DEFAULT 0,
    disposable_emails INT DEFAULT 0,
    valid_rate DECIMAL(5, 2) DEFAULT 0.00
);

COMMENT ON TABLE xtremand_production.xt_email_verification_batch IS 'Stores summary results for a batch of email verifications.';
COMMENT ON COLUMN xtremand_production.xt_email_verification_batch.id IS 'Primary key for the batch, using UUID.';
COMMENT ON COLUMN xtremand_production.xt_email_verification_batch.total_emails IS 'The total number of emails submitted in the batch.';
COMMENT ON COLUMN xtremand_production.xt_email_verification_batch.valid_emails IS 'Count of emails determined to be valid.';
COMMENT ON COLUMN xtremand_production.xt_email_verification_batch.valid_rate IS 'Percentage of valid emails in the batch (valid_emails / total_emails * 100).';


-- 2. Alter the existing history table to include batch tracking and domain information
ALTER TABLE xtremand_production.xt_user_email_verification_history
ADD COLUMN batch_id UUID,
ADD COLUMN domain VARCHAR(255);

-- 3. Add a foreign key constraint to link the history records to a specific batch
ALTER TABLE xtremand_production.xt_user_email_verification_history
ADD CONSTRAINT fk_email_verification_history_to_batch
FOREIGN KEY (batch_id)
REFERENCES xtremand_production.xt_email_verification_batch(id)
ON DELETE SET NULL; -- If a batch is deleted, we don't want to lose the history record

-- 4. Add indexes to optimize queries on the new columns
CREATE INDEX IF NOT EXISTS idx_email_verification_history_batch_id ON xtremand_production.xt_user_email_verification_history(batch_id);
CREATE INDEX IF NOT EXISTS idx_email_verification_history_domain ON xtremand_production.xt_user_email_verification_history(domain);
CREATE INDEX IF NOT EXISTS idx_email_verification_history_email_checked_at ON xtremand_production.xt_user_email_verification_history(email, checked_at DESC);