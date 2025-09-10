ALTER TABLE xt_user_activation_history
    ALTER COLUMN requested_at TYPE TIMESTAMP WITH TIME ZONE;

ALTER TABLE xt_user_activation_history
    ALTER COLUMN activated_at TYPE TIMESTAMP WITH TIME ZONE;
