package com.xtremand.user.repository;

import com.xtremand.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);

	boolean existsByEmailIgnoreCase(String emailAddress);

	Optional<User> findByEmailIgnoreCase(String email);

	Optional<User> findByUsername(String username);
}
