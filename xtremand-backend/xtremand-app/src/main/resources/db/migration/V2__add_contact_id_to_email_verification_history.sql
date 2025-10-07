ALTER TABLE xt_user_email_verification_history
ADD COLUMN contact_id BIGINT;

ALTER TABLE xt_user_email_verification_history
ADD CONSTRAINT fk_email_verification_history_contact
FOREIGN KEY (contact_id)
REFERENCES xt_contacts(id);