package com.xtremand.auth.config;

import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.web.SecurityFilterChain;

import com.xtremand.auth.handler.CustomAccessDeniedHandler;
import com.xtremand.auth.handler.CustomAuthenticationEntryPoint;

@Configuration
public class ResourceServerConfig {

	@Bean
	@Order(2)
	SecurityFilterChain resourceServerSecurity(HttpSecurity http, OpaqueTokenIntrospector introspector,
			CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
			CustomAccessDeniedHandler customAccessDeniedHandler) throws Exception {
		http.securityMatcher("/api/**")
				.csrf(csrf -> csrf.ignoringRequestMatchers("/api/auth/signup", "/api/auth/login"))
				.authorizeHttpRequests(
						auth -> auth.requestMatchers("/custom/token/**", "/api/auth/signup", "/api/auth/login")
								.permitAll().anyRequest().hasAuthority("SCOPE_read"))
				.oauth2ResourceServer(oauth2 -> oauth2.authenticationEntryPoint(customAuthenticationEntryPoint)
						.accessDeniedHandler(customAccessDeniedHandler)
						.opaqueToken(opaque -> opaque.introspector(introspector)));
		return http.build();
	}

}