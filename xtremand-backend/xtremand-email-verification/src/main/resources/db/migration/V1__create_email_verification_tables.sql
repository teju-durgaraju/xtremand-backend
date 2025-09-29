-- Create schema if it doesn't exist
CREATE SCHEMA IF NOT EXISTS xtremand_production;

-- Table to store validation rules (disposable domains, roles, blacklists)
CREATE TABLE IF NOT EXISTS xtremand_production.xt_email_validation_rules (
    id BIGSERIAL PRIMARY KEY,
    rule_type VARCHAR(50) NOT NULL, -- 'DISPOSABLE_DOMAIN', 'ROLE_BASED_PREFIX', 'BLACKLIST_DOMAIN', 'BLACKLIST_EMAIL', 'CATCH_ALL_DOMAIN'
    value VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_rule_type_value UNIQUE (rule_type, value)
);

-- Table to store the history of all email verification attempts
CREATE TABLE IF NOT EXISTS xtremand_production.xt_user_email_verification_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    email VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL, -- e.g., 'VALID', 'INVALID', 'RISKY', 'UNKNOWN'
    recommendation VARCHAR(50), -- e.g., 'ACCEPT', 'REVIEW', 'REJECT'
    score INT NOT NULL,
    confidence VARCHAR(50), -- e.g., 'HIGH', 'MEDIUM', 'LOW'

    -- Individual check results
    syntax_check BOOLEAN DEFAULT FALSE,
    mx_check BOOLEAN DEFAULT FALSE,
    disposable_check BOOLEAN DEFAULT FALSE,
    role_based_check BOOLEAN DEFAULT FALSE,
    blacklist_check BOOLEAN DEFAULT FALSE,
    catch_all_check BOOLEAN DEFAULT FALSE,

    -- SMTP specific results
    smtp_check_status VARCHAR(50) NOT NULL, -- e.g., 'DELIVERABLE', 'INVALID', 'CATCH_ALL', 'UNKNOWN', 'NOT_PERFORMED'
    smtp_ping_status VARCHAR(50) NOT NULL, -- e.g., 'SUCCESS', 'FAIL', 'NOT_PERFORMED'
    is_catch_all BOOLEAN DEFAULT FALSE,
    is_greylisted BOOLEAN DEFAULT FALSE,

    -- Metadata and logs
    details JSONB, -- For storing MX hosts, rule IDs, etc.
    smtp_logs TEXT, -- Sanitized SMTP session transcript

    checked_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_email_verification_email ON xtremand_production.xt_user_email_verification_history(email);
CREATE INDEX IF NOT EXISTS idx_email_verification_user_id ON xtremand_production.xt_user_email_verification_history(user_id);
CREATE INDEX IF NOT EXISTS idx_email_validation_rules_type ON xtremand_production.xt_email_validation_rules(rule_type);