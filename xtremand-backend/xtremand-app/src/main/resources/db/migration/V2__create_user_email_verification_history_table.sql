CREATE TABLE IF NOT EXISTS xt_user_email_verification_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    email VARCHAR(255) NOT NULL,
    score INT NOT NULL,
    confidence VARCHAR(10) NOT NULL,
    checked_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    syntax_check BOOLEAN NOT NULL,
    mx_check BOOLEAN NOT NULL,
    disposable_check BOOLEAN NOT NULL,
    role_based_check BOOLEAN NOT NULL,
    catch_all_check BOOLEAN NOT NULL,
    blacklist_check BOOLEAN NOT NULL,
    smtp_check BOOLEAN NOT NULL,
    FOREIGN KEY (user_id) REFERENCES xt_users(id) ON DELETE SET NULL
);

CREATE INDEX idx_email ON xt_user_email_verification_history(email);
CREATE INDEX idx_checked_at ON xt_user_email_verification_history(checked_at);
