package com.xtremand.email.verification.service.verifier;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Component;

@Component
public class SyntaxCheckProvider {

    private final EmailValidator validator;

    public SyntaxCheckProvider() {
        // Use a singleton instance of the validator
        this.validator = EmailValidator.getInstance();
    }

    /**
     * Validates the syntax of an email address.
     *
     * @param email The email address to validate.
     * @return true if the email has a valid format, false otherwise.
     */
    public boolean isValid(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return validator.isValid(email);
    }
}