-- Seed data for xt_email_validation_rules
-- Schema: xtremand_production

-- 1. Disposable Email Domains (a small, illustrative list)
INSERT INTO xtremand_production.xt_email_validation_rules (rule_type, value, description) VALUES
('DISPOSABLE_DOMAIN', '10minutemail.com', 'A popular temporary email provider'),
('DISPOSABLE_DOMAIN', 'temp-mail.org', 'A popular temporary email provider'),
('DISPOSABLE_DOMAIN', 'mailinator.com', 'A well-known disposable email service'),
('DISPOSABLE_DOMAIN', 'guerrillamail.com', 'A well-known disposable email service'),
('DISPOSABLE_DOMAIN', 'yopmail.com', 'A well-known disposable email service');

-- 2. Role-Based Email Prefixes
INSERT INTO xtremand_production.xt_email_validation_rules (rule_type, value, description) VALUES
('ROLE_BASED_PREFIX', 'admin', 'Administrative role'),
('ROLE_BASED_PREFIX', 'support', 'Support role'),
('ROLE_BASED_PREFIX', 'sales', 'Sales role'),
('ROLE_BASED_PREFIX', 'info', 'Information role'),
('ROLE_BASED_PREFIX', 'contact', 'Contact role'),
('ROLE_BASED_PREFIX', 'billing', 'Billing role'),
('ROLE_BASED_PREFIX', 'abuse', 'Abuse reporting role'),
('ROLE_BASED_PREFIX', 'postmaster', 'Postmaster role'),
('ROLE_BASED_PREFIX', 'hostmaster', 'Hostmaster role'),
('ROLE_BASED_PREFIX', 'webmaster', 'Webmaster role'),
('ROLE_BASED_PREFIX', 'noreply', 'No-reply address'),
('ROLE_BASED_PREFIX', 'security', 'Security role');

-- 3. Blacklisted Domains (example)
INSERT INTO xtremand_production.xt_email_validation_rules (rule_type, value, description) VALUES
('BLACKLIST_DOMAIN', 'spamdomain.com', 'Known source of spam'),
('BLACKLIST_DOMAIN', 'blockthis.org', 'Domain with poor reputation');

-- 4. Blacklisted Emails (example)
INSERT INTO xtremand_production.xt_email_validation_rules (rule_type, value, description) VALUES
('BLACKLIST_EMAIL', 'spammer@example.com', 'Known spammer email');

-- 5. Known Catch-All Domains (can be seeded here or detected dynamically)
-- Seeding a few known ones can save an SMTP check.
INSERT INTO xtremand_production.xt_email_validation_rules (rule_type, value, description) VALUES
('CATCH_ALL_DOMAIN', 'google.com', 'Google Workspace domains are often catch-all'),
('CATCH_ALL_DOMAIN', 'yahoo.com', 'Yahoo domains are often catch-all');