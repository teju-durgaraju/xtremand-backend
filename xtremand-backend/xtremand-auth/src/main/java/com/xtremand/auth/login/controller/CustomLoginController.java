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

	@PostMapping("/token")
	public ResponseEntity<TokenResponse> authenticateAndGenerateToken(@Valid @RequestBody LoginRequest loginRequest,
			HttpServletRequest request, @RequestHeader(value = "X-Client-Type", required = false) String clientType) {

		/*
		 * String ip = IpResolver.getClientIp(request); String username =
		 * loginRequest.getUsername(); LoginMetaData loginMetaData =
		 * loginMetaDataExtractor.extractFrom(request);
		 */

		//boolean ipLocked = rateLimiterService.isIpLocked(ip);
		//boolean userLocked = rateLimiterService.isUserLocked(username);

		/*
		 * if (ipLocked || userLocked) { LOGGER.warn("Login blocked for user={}, ip={}",
		 * username, ip); throw new AccountLockedException(userLocked, ipLocked); }
		 */
		Authentication authResult;
		try {
			authResult = authenticationService.authenticate(loginRequest, clientType);
		} catch (AuthenticationException ex) {
			// Record failure and send lockout email if threshold is hit
			//rateLimiterService.recordFailure(username, ip, "unknown", loginMetaData, clientType);
			throw ex;
		}

		//rateLimiterService.resetAttempts(username, ip);

		RegisteredClient registeredClient = oauth2Components.getRegisteredClientService()
				.findByClientId("xAmplify-Angular-Client");

		Map<String, Object> customAttributes = oauth2Components.getAttributeBuilder().build(loginRequest);

		OAuth2AccessToken accessToken = oauth2Components.getTokenService().generateAccessToken(authResult,
				registeredClient);
		OAuth2RefreshToken refreshToken = oauth2Components.getTokenService().generateRefreshToken(authResult,
				registeredClient);

		if (accessToken == null || refreshToken == null) {
			LOGGER.error("Token generation failed: accessToken or refreshToken is null");
			throw new TokenGenerationException();
		}

		if (environmentUtil.isLocal()) {
			accessToken = new OAuth2AccessToken(accessToken.getTokenType(), accessToken.getTokenValue(),
					accessToken.getIssuedAt(), Instant.now().plus(Duration.ofDays(3650)));
			refreshToken = new OAuth2RefreshToken(refreshToken.getTokenValue(), refreshToken.getIssuedAt(),
					Instant.now().plus(Duration.ofDays(3650)));
		}

		OAuth2Authorization authorization = oauth2Components.getAuthorizationBuilder().build(registeredClient,
				authResult, accessToken, refreshToken, customAttributes);

		oauth2Components.getAuthorizationService().save(authorization);
		oauth2Components.getOAuth2AuthorizationExtRepository().updateDeviceMetadataForCustomLogin(authorization.getId(),
				loginRequest);

		TokenResponse response = oauth2Components.getTokenResponseService().build(accessToken, refreshToken);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/refresh")
	public ResponseEntity<TokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
		final OAuth2Authorization authorization = oauth2Components.getAuthorizationService()
				.findByToken(request.getRefreshToken(), OAuth2TokenType.REFRESH_TOKEN);

		if (authorization == null) {
			LOGGER.warn("Invalid or expired refresh token: {}", request.getRefreshToken());
			throw new InvalidRefreshTokenException();
		}

		final RegisteredClient registeredClient = oauth2Components.getRegisteredClientService()
				.findById(authorization.getRegisteredClientId());

		final Authentication principal = new UsernamePasswordAuthenticationToken(authorization.getPrincipalName(), null,
				Collections.emptyList());

		final OAuth2AccessToken accessToken = oauth2Components.getTokenService()
				.generateAccessTokenFromRefresh(principal, registeredClient, authorization);
		final var refreshTokenHolder = authorization.getRefreshToken();
		if (refreshTokenHolder == null) {
			LOGGER.warn("Invalid or expired refresh token: {}", request.getRefreshToken());
			throw new InvalidRefreshTokenException();
		}
		final OAuth2RefreshToken refreshToken = refreshTokenHolder.getToken();

		final OAuth2Authorization updatedAuthorization = OAuth2Authorization.from(authorization)
				.accessToken(accessToken).refreshToken(refreshToken).build();

		oauth2Components.getAuthorizationService().save(updatedAuthorization);

		return ResponseEntity.ok(oauth2Components.getTokenResponseService().build(accessToken, refreshToken));
	}

}
