package com.xtremand.auth.oauth2.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.xtremand.auth.entity.RegisteredClientEntity;

public interface RegisteredClientEntityRepository extends JpaRepository<RegisteredClientEntity, String> {
	boolean existsByClientId(String clientId);
}
