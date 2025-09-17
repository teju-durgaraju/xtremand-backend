package com.xtremand.email.verification.dto;

import com.xtremand.domain.enums.Confidence;
import com.xtremand.domain.enums.VerificationStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailVerifierOutput {

    String email;
    VerificationStatus status;
    int score;
    Confidence confidence;
    EmailVerificationChecks checks;
    String message;

}
