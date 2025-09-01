package com.xtremand.domain.entity;

import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.xtremand.domain.enums.UserStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "xt_user", uniqueConstraints = {
		@UniqueConstraint(name = "uk_xt_user_email", columnNames = "email") }, indexes = {
				@Index(name = "idx_xt_user_email", columnList = "email"),
				@Index(name = "idx_xt_user_role_id", columnList = "role_id"),
				@Index(name = "idx_xt_user_created_at", columnList = "created_at"),
				@Index(name = "idx_xt_user_updated_at", columnList = "updated_at") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "xt_user_id_seq")
	@SequenceGenerator(name = "xt_user_id_seq", sequenceName = "xt_user_id_seq", allocationSize = 1)
	private Long id;

	@Builder.Default
	@Column(name = "external_id", nullable = false, unique = true, updatable = false)
	private UUID externalId = UUID.randomUUID();

	@NotNull
	@Email
	@Column(nullable = false, unique = true)
	private String email;

	@NotNull
	@Column(nullable = false, unique = true)
	private String username;

	@Column(nullable = false)
	private String password;

	@Builder.Default
	@Column(name = "is_email_verified", nullable = false)
	private boolean emailVerified = false;

	@Builder.Default
	@Convert(disableConversion = true)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(columnDefinition = "user_status", nullable = false)
	private UserStatus status = UserStatus.UNAPPROVED;

	@Builder.Default
	@Column(nullable = false)
	private boolean enabled = true;

	@Builder.Default
	@Column(name = "account_non_expired", nullable = false)
	private boolean accountNonExpired = true;

	@Builder.Default
	@Column(name = "account_non_locked", nullable = false)
	private boolean accountNonLocked = true;

	@Builder.Default
	@Column(name = "credentials_non_expired", nullable = false)
	private boolean credentialsNonExpired = true;

}
