-- Add user_id to the email verification batch table
ALTER TABLE xt_email_verification_batch
ADD COLUMN user_id BIGINT;

-- Backfill user_id for existing batches if possible (e.g., from the first history record)
-- This is a placeholder, as the actual logic might be more complex
UPDATE xt_email_verification_batch b
SET user_id = (
    SELECT h.user_id
    FROM xt_user_email_verification_history h
    WHERE h.batch_id = b.id
    ORDER BY h.checked_at
    LIMIT 1
)
WHERE user_id IS NULL;

-- Now that existing records are handled, add the NOT NULL constraint
ALTER TABLE xt_email_verification_batch
ALTER COLUMN user_id SET NOT NULL;

-- Add foreign key constraint for batch table
ALTER TABLE xt_email_verification_batch
ADD CONSTRAINT fk_batch_user FOREIGN KEY (user_id) REFERENCES xt_users(id);

-- Add foreign key constraint for history table
-- The user_id column already exists, but we'll ensure it's not null and has the FK
ALTER TABLE xt_user_email_verification_history
ADD CONSTRAINT fk_history_user FOREIGN KEY (user_id) REFERENCES xt_users(id);

-- Set the user_id column to NOT NULL in the history table
-- Note: This assumes all existing history records have a valid user_id.
-- If not, a backfill similar to the batch table would be needed here first.
ALTER TABLE xt_user_email_verification_history
ALTER COLUMN user_id SET NOT NULL;