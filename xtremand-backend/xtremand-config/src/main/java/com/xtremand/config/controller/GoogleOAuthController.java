package com.xtremand.config.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.view.RedirectView;

import com.xtremand.auth.oauth2.service.AuthenticationFacade;
import com.xtremand.config.MailConfigService;
import com.xtremand.domain.dto.GoogleUserInfo;
import com.xtremand.domain.dto.MailConfigInput;
import com.xtremand.domain.dto.OAuthTokenResponse;
import com.xtremand.domain.enums.EmailConfigType;

import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
@RestController
@RequestMapping("/social")
public class GoogleOAuthController {

    @Value("${google.client.id}")
    private String googleClientId;

    @Value("${google.client.secret}")
    private String googleClientSecret;

    @Value("${google.oauth.redirect-uri}")
    private String googleRedirectUri;

    private final WebClient webClient = WebClient.create();

    private final MailConfigService mailConfigService;
    
    private final AuthenticationFacade authenticationFacade;

    @Autowired
    public GoogleOAuthController(MailConfigService mailConfigService,AuthenticationFacade authenticationFacade) {
        this.mailConfigService = mailConfigService;
        this.authenticationFacade =  authenticationFacade;
    }

    // Redirect user to Google OAuth consent screen
    @GetMapping("/connect/google")
    public RedirectView redirectToGoogleOAuth() {
        String url = "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=" + URLEncoder.encode(googleClientId, StandardCharsets.UTF_8) +
                "&redirect_uri=" + URLEncoder.encode(googleRedirectUri, StandardCharsets.UTF_8) +
                "&response_type=code" +
                "&scope=" + URLEncoder.encode("https://mail.google.com/ https://www.googleapis.com/auth/userinfo.email", StandardCharsets.UTF_8) +
                "&access_type=offline" +
                "&prompt=consent";
        return new RedirectView(url);
    }

    // OAuth2 callback from Google
    @GetMapping("/oauth2/callback/google")
    public Mono<String> handleGoogleCallback(@RequestParam("code") String code) {
        return exchangeCodeForTokens(code)
            .flatMap(tokens -> fetchUserEmail(tokens.getAccessToken())
                .map(email -> {
                    MailConfigInput input = new MailConfigInput();
                    input.setEmail(email);
                    input.setConfigType(EmailConfigType.OAUTH_CONFIG.toString());
                    input.setOauthToken(tokens.getAccessToken());
                    input.setOauthRefreshToken(tokens.getRefreshToken());
                    input.setTokenExpiry(LocalDateTime.now().plusDays(30));
                    input.setUsername(email);
                    mailConfigService.saveOrUpdateMailConfig(input,null);
                    return "Google account " + email + " connected and saved successfully!";
                }));
    }


    // Exchange authorization code for tokens
    private Mono<OAuthTokenResponse> exchangeCodeForTokens(String code) {
        return webClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("code", code)
                        .with("client_id", googleClientId)
                        .with("client_secret", googleClientSecret)
                        .with("redirect_uri", googleRedirectUri)
                        .with("grant_type", "authorization_code"))
                .retrieve()
                .bodyToMono(OAuthTokenResponse.class);
    }

    // Fetch user email from Google's userinfo endpoint
    private Mono<String> fetchUserEmail(String accessToken) {
        return webClient.get()
                .uri("https://www.googleapis.com/oauth2/v2/userinfo")
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(GoogleUserInfo.class)
                .map(GoogleUserInfo::getEmail);
    }
}
