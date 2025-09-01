package com.xtremand.auth.oauth2.service;

import java.util.List;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

public class CustomOAuth2AuthorizationService extends JdbcOAuth2AuthorizationService {

	public CustomOAuth2AuthorizationService(JdbcOperations jdbcOperations,
			RegisteredClientRepository registeredClientRepository) {
		super(jdbcOperations, registeredClientRepository);
	}

	public OAuth2Authorization findByClientIdAndPrincipal(String registeredClientId, String principalName) {
		String sql = "SELECT id FROM oauth2_authorization WHERE registered_client_id = ? AND principal_name = ?";
		List<String> ids = getJdbcOperations().query(sql, (rs, rowNum) -> rs.getString("id"), registeredClientId,
				principalName);

		if (ids.isEmpty())
			return null;
		return this.findById(ids.get(0)); // You could also return a list if needed
	}
}