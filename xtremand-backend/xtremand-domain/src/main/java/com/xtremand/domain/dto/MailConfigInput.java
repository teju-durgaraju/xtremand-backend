package com.xtremand.domain.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MailConfigInput {
    private String configType;   
    private String smtpHost;
    private Integer smtpPort;
    private String email;
    private String username;
    private String password;
    private String oauthToken;
    private String oauthRefreshToken;
    private LocalDateTime tokenExpiry;
    private String displayName;
}

