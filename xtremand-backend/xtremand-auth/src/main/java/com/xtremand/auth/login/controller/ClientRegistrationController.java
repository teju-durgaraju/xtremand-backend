package com.xtremand.auth.login.controller;

import java.time.Instant;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.auth.entity.RegisteredClientEntity;
import com.xtremand.auth.oauth2.model.ClientType;
import com.xtremand.auth.oauth2.repository.RegisteredClientEntityRepository;

@RestController
@RequestMapping("/public/client-registration")
@CrossOrigin(origins = "http://localhost:4200")
public class ClientRegistrationController {

    private final StringKeyGenerator clientIdGen = new Base64StringKeyGenerator(32); // 32-byte base64 key
    private final StringKeyGenerator clientSecretGen = new Base64StringKeyGenerator(48); // 48-byte base64 key
    private final PasswordEncoder passwordEncoder;
    private RegisteredClientEntityRepository registeredClientEntityRepository;

    public ClientRegistrationController(RegisteredClientEntityRepository registeredClientEntityRepository,
            PasswordEncoder passwordEncoder) {
        this.registeredClientEntityRepository = registeredClientEntityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    public ResponseEntity<?> registerClient(@RequestBody RegisteredClientEntity input) {
        RegisteredClientEntity client = new RegisteredClientEntity();
        String rawSecret = clientSecretGen.generateKey();
        String hashedSecret = passwordEncoder.encode(rawSecret); // BCrypt, recommended
        client.setClientSecret(hashedSecret);
        client.setRegistrationId(toSlug(input.getClientName()));
        client.setId(UUID.randomUUID().toString());
        client.setClientId(clientIdGen.generateKey());
        //client.setClientSecret(clientSecretGen.generateKey());
        client.setClientName(input.getClientName());
        client.setRedirectUris(input.getRedirectUris());
        client.setScopes(input.getScopes() != null ? input.getScopes() : "openid,email,profile");
        client.setAuthorizationGrantTypes("authorization_code");
        client.setClientAuthenticationMethods("client_secret_basic");
        client.setClientIdIssuedAt(Instant.now());
        client.setRegistrationId(input.getClientName().toLowerCase().replace(" ", "-"));
        client.setClientType(ClientType.CONFIDENTIAL_INTERNAL);
        client.setClientSettings("""
                  {
                    "settings.client.require-proof-key": false,
                    "settings.client.require-authorization-consent": true
                  }
                """);

        client.setTokenSettings("""
                  {
                    "settings.token.access-token-time-to-live": "PT15M",
                    "settings.token.refresh-token-time-to-live": "P30D",
                    "settings.token.reuse-refresh-tokens": true
                  }
                """);

        registeredClientEntityRepository.save(client);

        return ResponseEntity.ok(new ClientResponse(client.getClientId(), rawSecret, client.getClientName()));
    }

    record ClientResponse(String clientId, String clientSecret, String clientName) {
    }

    public static String toSlug(String input) {
        return input.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}
