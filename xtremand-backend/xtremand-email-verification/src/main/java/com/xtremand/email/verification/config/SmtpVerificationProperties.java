package com.xtremand.email.verification.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

@Configuration
@ConfigurationProperties(prefix = "xtremand.email.verification.smtp")
@Getter
@Setter
@Validated
public class SmtpVerificationProperties {

    /**
     * Enable or disable the SMTP probe check globally.
     */
    private boolean enabled = true;

    /**
     * The email address to use in the 'MAIL FROM' command.
     */
    @NotEmpty
    private String mailFrom = "verify@xtremand.com";

    /**
     * The hostname to use in the 'EHLO' command.
     */
    @NotEmpty
    private String heloHost = "xtremand.com";

    /**
     * Connection timeout in milliseconds.
     */
    @Min(100)
    private int connectionTimeout = 5000;

    /**
     * Read timeout in milliseconds for SMTP commands.
     */
    @Min(100)
    private int readTimeout = 5000;

    /**
     * Number of retries to attempt if a greylisting response (4xx) is received.
     */
    @Min(0)
    private int greylistingRetries = 1;

    /**
     * Initial delay in milliseconds before the first greylisting retry.
     */
    @Min(100)
    private long greylistingRetryDelay = 1000;
}