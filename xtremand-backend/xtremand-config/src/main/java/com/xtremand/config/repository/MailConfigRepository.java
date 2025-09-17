package com.xtremand.config.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.xtremand.domain.dto.MailConfigDTO;
import com.xtremand.domain.entity.MailConfig;
import com.xtremand.domain.entity.User;

@Repository
public interface MailConfigRepository extends JpaRepository<MailConfig, Long> {

	Optional<MailConfig> findById(Long id);

	Optional<MailConfig> findByEmail(String email);

	Optional<MailConfig> findByCreatedBy(User user);

	@Query("SELECT new com.xtremand.domain.dto.MailConfigDTO(m.id, m.configType, m.email, m.displayName, "
			+ "m.username, m.password, m.oauthAccessToken, m.createdAt, m.updatedAt, m.host, m.port, "
			+ "m.oauthRefreshToken, m.tokenExpiry) " + "FROM MailConfig m WHERE m.createdBy = :user")
	Optional<MailConfigDTO> findDtoByCreatedBy(@Param("user") User user);

}
