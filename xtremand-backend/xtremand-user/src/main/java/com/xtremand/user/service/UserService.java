package com.xtremand.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.xtremand.common.util.AESUtil;
import com.xtremand.common.dto.UserProfile;
import com.xtremand.domain.entity.User;
import com.xtremand.user.dto.SignupRequest;
import com.xtremand.user.repository.UserRepository;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final String secretKey;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
			@Value("${app.auth.secret-key}") String secretKey) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.secretKey = secretKey;

	}

	public UserProfile register(SignupRequest request) {
		/*
		 * userRepository.findByEmail(request.getEmail()).ifPresent(u -> { throw new
		 * IllegalArgumentException("Email already in use"); }); String decrypted =
		 * AESUtil.decrypt(request.getPassword()); User user = User.builder()
		 * .username(request.getFullName()) .email(request.getEmail())
		 * .password(passwordEncoder.encode(decrypted)) .build();
		 * userRepository.save(user); return UserProfile.builder() .id(user.getId())
		 * .email(user.getEmail()) .fullName(user.getUsername()) .role(user.getRole() !=
		 * null ? user.getRole().getName().name() : null) .build();
		 */
		return null;}
}
