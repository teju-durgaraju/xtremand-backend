package com.xtremand.email.verification.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class VerifyBatchRequest {

    @NotEmpty(message = "Email list cannot be empty.")
    @Size(max = 1000, message = "Batch size cannot exceed 1000 emails.")
    private List<String> emails;
}