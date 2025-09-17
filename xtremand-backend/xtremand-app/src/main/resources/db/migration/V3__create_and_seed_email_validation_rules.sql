CREATE TABLE IF NOT EXISTS xt_email_validation_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_type VARCHAR(20) NOT NULL,
    value VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Seed Data for Disposable Domains
INSERT INTO xt_email_validation_rules (rule_type, value) VALUES
('DISPOSABLE', 'mailinator.com'),
('DISPOSABLE', 'tempmail.com'),
('DISPOSABLE', '10minutemail.com'),
('DISPOSABLE', 'guerrillamail.com'),
('DISPOSABLE', 'yopmail.com'),
('DISPOSABLE', 'trashmail.com'),
('DISPOSABLE', 'getnada.com'),
('DISPOSABLE', 'burnermail.io'),
('DISPOSABLE', 'dispostable.com'),
('DISPOSABLE', 'sharklasers.com');

-- Seed Data for Role-based Prefixes
INSERT INTO xt_email_validation_rules (rule_type, value) VALUES
('ROLE_BASED', 'admin@'),
('ROLE_BASED', 'support@'),
('ROLE_BASED', 'contact@'),
('ROLE_BASED', 'info@'),
('ROLE_BASED', 'help@'),
('ROLE_BASED', 'hr@'),
('ROLE_BASED', 'jobs@'),
('ROLE_BASED', 'sales@'),
('ROLE_BASED', 'billing@'),
('ROLE_BASED', 'postmaster@'),
('ROLE_BASED', 'webmaster@'),
('ROLE_BASED', 'noreply@');

-- Seed Data for Catch-all Domains
INSERT INTO xt_email_validation_rules (rule_type, value) VALUES
('CATCH_ALL', 'examplecatchall.com'),
('CATCH_ALL', 'mailcatchall.org'),
('CATCH_ALL', 'testdomain.net');

-- Seed Data for Blacklisted Emails
INSERT INTO xt_email_validation_rules (rule_type, value) VALUES
('BLACKLIST_EMAIL', 'fraudulent@example.com'),
('BLACKLIST_EMAIL', 'testspam@gmail.com'),
('BLACKLIST_EMAIL', 'botaccount@yahoo.com');

-- Seed Data for Blacklisted Domains
INSERT INTO xt_email_validation_rules (rule_type, value) VALUES
('BLACKLIST_DOMAIN', 'spammail.com'),
('BLACKLIST_DOMAIN', 'fakeinbox.net'),
('BLACKLIST_DOMAIN', 'malicious.org');
