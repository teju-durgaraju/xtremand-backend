package com.xtremand.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xtremand.domain.entity.UserActivationHistory;

@Repository
public interface UserActivationHistoryRepository extends JpaRepository<UserActivationHistory, Long> {

    Optional<UserActivationHistory> findByActivationToken(String activationToken);
}
