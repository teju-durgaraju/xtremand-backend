package com.xtremand.email.verification.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyEmailRequest {

    @NotEmpty(message = "Email must not be empty.")
    @Email(message = "Email should be valid.")
    private String email;

    private boolean forceSmtp = false; // Not yet implemented, but here for future use
}