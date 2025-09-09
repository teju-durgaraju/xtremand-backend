package com.xtremand.auth.login.controller;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
// Removed unused import OAuth2Token
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
// Removed unused import OAuth2TokenGenerator
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Removed unused imports for user audit
import com.xtremand.auth.login.dto.LoginRequest;
import com.xtremand.auth.login.dto.RefreshTokenRequest;
import com.xtremand.auth.login.dto.TokenResponse;
import com.xtremand.auth.login.exception.InvalidRefreshTokenException;
import com.xtremand.auth.login.exception.TokenGenerationException;
//import com.xtremand.auth.login.service.LoginRateLimiterService;
import com.xtremand.auth.oauth2.customlogin.service.AuthenticationService;
import com.xtremand.auth.oauth2.customlogin.service.OAuth2LoginComponents;
import com.xtremand.common.environment.EnvironmentUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class CustomLoginController {

	private final AuthenticationService authenticationService;
	private final OAuth2LoginComponents oauth2Components;
	private final EnvironmentUtil environmentUtil;
	//private final LoginRateLimiterService rateLimiterService;
	//private final LoginMetaDataExtractor loginMetaDataExtractor;
	// Removed userRepository and loginAuditService fields as they are unused

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomLoginController.class);


}
