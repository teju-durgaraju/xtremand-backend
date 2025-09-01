package com.xtremand.email.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xtremand.domain.entity.ContactListEmailAnalytics;

@Repository
public interface ContactListEmailAnalyticsRepository extends JpaRepository<ContactListEmailAnalytics, Long> {
}

