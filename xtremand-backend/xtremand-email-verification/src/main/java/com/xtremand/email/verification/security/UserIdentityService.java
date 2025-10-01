package com.xtremand.email.verification.security;

import com.xtremand.auth.userdetails.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserIdentityService {

    public Optional<Long> getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return Optional.of(userDetails.getUserId());
        } else if (principal instanceof Jwt jwt) {
            return Optional.ofNullable(jwt.getClaim("user_id"));
        }

        return Optional.empty();
    }

    public Long getRequiredUserId() {
        return getUserId().orElseThrow(() -> new IllegalStateException("User ID not found in security context"));
    }
}