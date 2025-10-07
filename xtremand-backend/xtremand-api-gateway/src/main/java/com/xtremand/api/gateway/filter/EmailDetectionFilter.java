package com.xtremand.api.gateway.filter;

import com.xtremand.api.gateway.service.EmailVerifierService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailDetectionFilter extends OncePerRequestFilter {

    private final EmailVerifierService emailVerifierService;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b"
    );
    private static final Set<String> SUPPORTED_METHODS = new HashSet<>(Arrays.asList("POST", "PUT", "PATCH"));

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (SUPPORTED_METHODS.contains(request.getMethod())) {
            ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
            filterChain.doFilter(requestWrapper, response);

            byte[] requestBody = requestWrapper.getContentAsByteArray();
            String bodyString = new String(requestBody, StandardCharsets.UTF_8);

            if (!bodyString.isEmpty()) {
                Set<String> emails = extractEmails(bodyString);
                if (!emails.isEmpty()) {
                    log.info("Detected {} emails in request to [{}]. Triggering async verification.",
                            emails.size(), request.getRequestURI());
                    emailVerifierService.verifyEmails(emails);
                }
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private Set<String> extractEmails(String content) {
        Set<String> emails = new HashSet<>();
        if (content == null || content.isEmpty()) {
            return emails;
        }
        Matcher matcher = EMAIL_PATTERN.matcher(content);
        while (matcher.find()) {
            emails.add(matcher.group());
        }
        return emails;
    }
}