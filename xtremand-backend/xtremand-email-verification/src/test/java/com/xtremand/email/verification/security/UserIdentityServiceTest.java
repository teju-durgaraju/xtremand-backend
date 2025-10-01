package com.xtremand.email.verification.security;

import com.xtremand.auth.userdetails.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserIdentityServiceTest {

    @InjectMocks
    private UserIdentityService userIdentityService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getUserId_fromCustomUserDetails_shouldReturnUserId() {
        // Given
        long expectedUserId = 123L;
        CustomUserDetails userDetails = new CustomUserDetails(expectedUserId, "testuser", "password", true, null);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // When
        Optional<Long> actualUserId = userIdentityService.getUserId();

        // Then
        assertTrue(actualUserId.isPresent());
        assertEquals(expectedUserId, actualUserId.get());
    }

    @Test
    void getUserId_fromJwt_shouldReturnUserId() {
        // Given
        long expectedUserId = 456L;
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").claim("user_id", expectedUserId).build();
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(jwt);

        // When
        Optional<Long> actualUserId = userIdentityService.getUserId();

        // Then
        assertTrue(actualUserId.isPresent());
        assertEquals(expectedUserId, actualUserId.get());
    }

    @Test
    void getUserId_whenNotAuthenticated_shouldReturnEmpty() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // When
        Optional<Long> actualUserId = userIdentityService.getUserId();

        // Then
        assertFalse(actualUserId.isPresent());
    }

    @Test
    void getUserId_whenPrincipalIsUnsupported_shouldReturnEmpty() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(new Object()); // Unsupported principal type

        // When
        Optional<Long> actualUserId = userIdentityService.getUserId();

        // Then
        assertFalse(actualUserId.isPresent());
    }

    @Test
    void getRequiredUserId_whenUserExists_shouldReturnId() {
        // Given
        long expectedUserId = 123L;
        CustomUserDetails userDetails = new CustomUserDetails(expectedUserId, "testuser", "password", true, null);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // When
        Long actualUserId = userIdentityService.getRequiredUserId();

        // Then
        assertEquals(expectedUserId, actualUserId);
    }

    @Test
    void getRequiredUserId_whenUserDoesNotExist_shouldThrowException() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        // When & Then
        assertThrows(IllegalStateException.class, () -> userIdentityService.getRequiredUserId());
    }
}