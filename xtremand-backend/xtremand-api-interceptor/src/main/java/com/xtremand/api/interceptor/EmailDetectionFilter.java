package com.xtremand.api.interceptor;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class EmailDetectionFilter extends OncePerRequestFilter {

    private final EmailVerifierService emailVerifierService;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}");
    private static final Set<String> SUPPORTED_METHODS = new HashSet<>(Arrays.asList("POST", "PUT", "PATCH"));

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!SUPPORTED_METHODS.contains(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);
        String requestBody = new String(cachedRequest.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        if (requestBody != null && !requestBody.isEmpty()) {
            Matcher matcher = EMAIL_PATTERN.matcher(requestBody);
            Set<String> emails = new HashSet<>();
            while (matcher.find()) {
                emails.add(matcher.group());
            }

            if (!emails.isEmpty()) {
                log.info("Detected {} emails in request body. Triggering async verification.", emails.size());
                emailVerifierService.verifyEmails(emails);
            }
        }

        filterChain.doFilter(cachedRequest, response);
    }
}