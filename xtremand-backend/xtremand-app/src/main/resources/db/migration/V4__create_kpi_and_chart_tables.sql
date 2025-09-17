CREATE TABLE IF NOT EXISTS xt_email_verification_kpi (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    month VARCHAR(7) NOT NULL,
    valid_emails INT NOT NULL,
    invalid_emails INT NOT NULL,
    risky_emails INT NOT NULL,
    unknown_emails INT NOT NULL,
    total_processed INT NOT NULL,
    quality_score DECIMAL(5, 2) NOT NULL,
    deliverability_rate DECIMAL(5, 2) NOT NULL,
    bounce_rate DECIMAL(5, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS xt_email_verification_chart_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    period VARCHAR(255) NOT NULL,
    verified INT NOT NULL,
    deliverable INT NOT NULL,
    risky INT NOT NULL,
    invalid INT NOT NULL,
    unknown INT NOT NULL,
    total INT NOT NULL,
    aggregation_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
