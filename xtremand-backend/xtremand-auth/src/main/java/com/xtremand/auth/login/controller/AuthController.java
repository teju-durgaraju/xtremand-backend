package com.xtremand.auth.login.controller;

import com.xtremand.auth.login.dto.LoginRequest;
import com.xtremand.auth.login.dto.TokenResponse;
import com.xtremand.common.dto.UserProfile;
import com.xtremand.user.dto.SignupRequest;
import com.xtremand.auth.login.dto.LoginRequest;
import com.xtremand.auth.login.dto.TokenResponse;
import com.xtremand.auth.login.exception.TokenGenerationException;
import com.xtremand.auth.oauth2.customlogin.service.AuthenticationService;
import com.xtremand.auth.oauth2.customlogin.service.OAuth2LoginComponents;
import com.xtremand.common.dto.UserProfile;
import com.xtremand.user.dto.SignupRequest;
import com.xtremand.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User signup and login")
public class AuthController {

    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final OAuth2LoginComponents oauth2Components;

    public AuthController(UserService userService, AuthenticationService authenticationService, OAuth2LoginComponents oauth2Components) {
        this.userService = userService;
        this.authenticationService = authenticationService;
        this.oauth2Components = oauth2Components;
    }

    @PostMapping("/signup")
    @Operation(summary = "Register a new user", description = "Creates a new user and returns their profile information.")
    @ApiResponse(responseCode = "200", description = "User registered successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public UserProfile signup(@Validated @RequestBody SignupRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate a user", description = "Authenticates a user and returns an access token and refresh token.")
    @ApiResponse(responseCode = "200", description = "User authenticated successfully")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    public ResponseEntity<TokenResponse> login(@Validated @RequestBody LoginRequest request) {
        Authentication authResult = authenticationService.authenticate(request, "web");

        RegisteredClient registeredClient = oauth2Components.getRegisteredClientService()
                .findByClientId("xtremand-Web-Client");

        Map<String, Object> customAttributes = oauth2Components.getAttributeBuilder().build(request);

        OAuth2AccessToken accessToken = oauth2Components.getTokenService().generateAccessToken(authResult,
                registeredClient);
        OAuth2RefreshToken refreshToken = oauth2Components.getTokenService().generateRefreshToken(authResult,
                registeredClient);

        if (accessToken == null || refreshToken == null) {
            throw new TokenGenerationException();
        }

        OAuth2Authorization authorization = oauth2Components.getAuthorizationBuilder().build(registeredClient,
                authResult, accessToken, refreshToken, customAttributes);

        oauth2Components.getAuthorizationService().save(authorization);

        TokenResponse response = oauth2Components.getTokenResponseService().build(accessToken, refreshToken);
        return ResponseEntity.ok(response);
    }
}
