CREATE TABLE xt_user_activation_history (
    id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    activation_token VARCHAR(255) NOT NULL,
    requested_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    activated_at TIMESTAMP WITHOUT TIME ZONE,
    status VARCHAR(255) NOT NULL,
    CONSTRAINT pk_xt_user_activation_history PRIMARY KEY (id)
);

ALTER TABLE xt_user_activation_history ADD CONSTRAINT FK_XT_USER_ACTIVATION_HISTORY_ON_USER FOREIGN KEY (user_id) REFERENCES xt_users (id);

CREATE SEQUENCE xt_user_activation_history_id_seq START 1;
