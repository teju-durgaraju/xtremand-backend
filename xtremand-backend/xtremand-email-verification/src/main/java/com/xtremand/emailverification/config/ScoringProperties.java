package com.xtremand.emailverification.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "xtremand.email.verification.scoring")
@Getter
@Setter
@Validated
public class ScoringProperties {

    @Getter
    @Setter
    public static class Weights {
        @Min(0) private int syntaxValid = 20;
        @Min(0) private int mxValid = 20;
        @Min(0) private int notDisposable = 15;
        @Min(0) private int notRoleBased = 10;
        @Min(0) private int notCatchAll = 10;
        @Min(0) private int notBlacklisted = 15;
        @Min(0) private int smtpDeliverable = 10;
        @Min(0) private int smtpPingSuccess = 5;
    }

    @Getter
    @Setter
    public static class Thresholds {
        @Min(0) @Max(100) private int confidenceHigh = 90;
        @Min(0) @Max(100) private int confidenceMedium = 70;
    }

    private Weights weights = new Weights();
    private Thresholds thresholds = new Thresholds();
}