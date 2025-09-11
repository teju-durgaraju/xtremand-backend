package com.xtremand.auth.config;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.JdbcOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

import com.xtremand.auth.handler.CustomAccessDeniedHandler;
import com.xtremand.auth.handler.CustomAuthenticationEntryPoint;
import com.xtremand.auth.login.provider.CustomLoginAuthenticationProvider;
import com.xtremand.auth.oauth2.handler.CustomOAuth2SuccessHandler;
import com.xtremand.auth.userdetails.CustomUserDetailsServiceImpl;
import com.xtremand.common.environment.EnvironmentUtil;
import com.xtremand.common.identity.UserLookupService;
import com.xtremand.common.util.AESKeyHolder;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	private final CorsConfigurationSource corsConfigurationSource;

	public SecurityConfig(CorsConfigurationSource corsConfigurationSource) {
		this.corsConfigurationSource = corsConfigurationSource;
	}

	@Bean
	@Order(1)
	SecurityFilterChain apiDocsSecurityFilterChain(HttpSecurity http) throws Exception {
		http.securityMatcher("/v3/api-docs").authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
				.httpBasic(Customizer.withDefaults()).csrf(csrf -> csrf.disable());
		return http.build();
	}

	@Bean
	@Order(3)
	SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http, CustomOAuth2SuccessHandler successHandler,
			CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
			CustomAccessDeniedHandler customAccessDeniedHandler) throws Exception {

		http.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/", "/index.html", "/auth/login", "/auth/refresh", "/error",
								"/css/**", "/js/**", "/images/**", "/custom/token/**", "/public/**", "/assets/**",
								"/actuator/**", "/docs.html", "/openapi.json", "/swagger-ui/**", "/v3/api-docs/**",
								"/favicon.ico", "/api/v1/account-activations", "/debug/**", "/auth/signup","/activate**")
						.permitAll().anyRequest().authenticated())
				.formLogin(Customizer.withDefaults()).oauth2Login(oauth -> oauth.successHandler(successHandler))
				.exceptionHandling(eh -> eh.authenticationEntryPoint(customAuthenticationEntryPoint)
						.accessDeniedHandler(customAccessDeniedHandler))
				.cors(cors -> cors.configurationSource(corsConfigurationSource)).logout(logout -> logout
						.logoutUrl("/logout").logoutSuccessHandler((request, response, authentication) -> {
							response.setStatus(HttpServletResponse.SC_OK);
							response.setContentType("application/json");
							response.getWriter().write("{\"message\": \"Logout successful\"}");
						}).invalidateHttpSession(true).clearAuthentication(true).deleteCookies("JSESSIONID"));

		return http.build();
	}

	@Bean
	OpaqueTokenIntrospector customIntrospector(OAuth2AuthorizationService authorizationService,
			RegisteredClientRepository registeredClientRepository, EnvironmentUtil environmentUtil,
			com.xtremand.common.identity.UserLookupService userLookupService) {
		// In 'dev' profile, skip expiration checks to ease local testing
		boolean skipExpiryCheck = environmentUtil.isDev();

		return token -> {
			OAuth2Authorization authorization = authorizationService.findByToken(token, OAuth2TokenType.ACCESS_TOKEN);
			if (authorization == null || authorization.getAccessToken() == null) {
				throw new OAuth2AuthenticationException("Invalid access token");
			}
			// Conditionally enforce expiration
			if (!skipExpiryCheck) {
				Instant now = Instant.now();
				Instant expiresAt = authorization.getAccessToken().getToken().getExpiresAt();
				if (expiresAt == null || expiresAt.isBefore(now)) {
					throw new OAuth2AuthenticationException("Access token expired");
				}
			}

			String principalName = authorization.getPrincipalName();
			Set<String> authorizedScopes = authorization.getAuthorizedScopes();
			List<GrantedAuthority> authorities = new ArrayList<>();
			authorities.addAll(
					authorizedScopes.stream().map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope)).toList());

			Map<String, Object> attributes = new HashMap<>(authorization.getAttributes());

			userLookupService.findByEmail(principalName).ifPresent(user -> {
				attributes.put("user_id", user.getId());

				if (user.getRoles() != null) {
					user.getRoles().forEach(r -> authorities.add(new SimpleGrantedAuthority(r)));
				}
				if (user.getPrivileges() != null) {
					user.getPrivileges().forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
				}
			});
			return new DefaultOAuth2AuthenticatedPrincipal(principalName, attributes, authorities);
		};
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	OAuth2AuthorizedClientService authorizedClientService(ClientRegistrationRepository clientRegistrationRepository,
			JdbcOperations jdbcOperations) {
		return new JdbcOAuth2AuthorizedClientService(jdbcOperations, clientRegistrationRepository);
	}

	@Bean
	AuthenticationManager authenticationManager(CustomLoginAuthenticationProvider customLoginAuthenticationProvider) {
		return new ProviderManager(customLoginAuthenticationProvider);
	}

	@Bean
	JdbcUserDetailsManager jdbcUserDetailsManager(DataSource dataSource) {
		return new JdbcUserDetailsManager(dataSource);
	}

	@PostConstruct
	public void initKey() {
		AESKeyHolder.initialize("changeit12345678");
	}

}
