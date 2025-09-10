CREATE TABLE xt_user_forgot_password_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    reset_token VARCHAR(255) NOT NULL,
    requested_at TIMESTAMP NOT NULL,
    reset_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES xt_users(id)
);

CREATE INDEX idx_reset_token ON xt_user_forgot_password_history(reset_token);
