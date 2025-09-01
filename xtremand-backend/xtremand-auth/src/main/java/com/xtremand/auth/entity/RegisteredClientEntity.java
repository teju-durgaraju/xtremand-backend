package com.xtremand.auth.entity;

import java.time.Instant;

import com.xtremand.auth.oauth2.model.ClientType;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "oauth2_registered_client")
@Data
public class RegisteredClientEntity {

	@Id
	private String id;

	private String registrationId;

	private String clientId;

	private String clientSecret;

	private String clientName;

	private String authorizationGrantTypes;

	private String clientAuthenticationMethods;

	private String redirectUris;

	private String scopes;

	private String clientSettings;

	private String tokenSettings;

	private Instant clientIdIssuedAt;

	private ClientType clientType;

}
