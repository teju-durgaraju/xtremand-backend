package com.xtremand.email.verification.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailVerificationChecks {
    boolean syntaxCheck;
    boolean mxCheck;
    boolean disposableCheck;
    boolean roleBasedCheck;
    boolean catchAllCheck;
    boolean blacklistCheck;
    boolean smtpCheck;
}
