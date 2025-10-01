package com.xtremand.email.verification;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {"com.xtremand.domain.entity"})
@EnableJpaRepositories(basePackages = {"com.xtremand.email.verification.repository", "com.xtremand.user.repository"})
public class TestEmailVerificationApplication {
    public static void main(String[] args) {
    }
}