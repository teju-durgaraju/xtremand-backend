package com.xtremand.api.gateway.filter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.xtremand.api.gateway.service.EmailVerifierService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class EmailDetectionFilter extends OncePerRequestFilter {

	private final EmailVerifierService emailVerifierService;

	public EmailDetectionFilter(EmailVerifierService emailVerifierService) {
		this.emailVerifierService = emailVerifierService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		// Apply only for POST/PUT/ PATCH with JSON
		if (!"POST".equalsIgnoreCase(request.getMethod()) && !"PUT".equalsIgnoreCase(request.getMethod())
				&& !"PATCH".equalsIgnoreCase(request.getMethod())) {
			filterChain.doFilter(request, response);
			return;
		}

		CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(request);
		String body = new String(wrappedRequest.getInputStream().readAllBytes());

		Set<String> emails = extractEmails(body);

		// Fire async verification (non-blocking)
		emailVerifierService.verifyEmails(emails);

		// Continue API execution immediately
		filterChain.doFilter(wrappedRequest, response);
	}

	private Set<String> extractEmails(String jsonBody) {
		Set<String> emails = new HashSet<>();
		Matcher matcher = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}").matcher(jsonBody);
		while (matcher.find()) {
			emails.add(matcher.group());
		}
		return emails;
	}
}
