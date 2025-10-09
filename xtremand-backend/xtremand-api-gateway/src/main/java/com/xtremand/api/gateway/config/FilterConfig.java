package com.xtremand.api.gateway.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.xtremand.api.gateway.filter.EmailDetectionFilter;

@Configuration
public class FilterConfig {

	@Bean
	public FilterRegistrationBean<EmailDetectionFilter> emailDetectionFilterCustom(EmailDetectionFilter filter) {
		FilterRegistrationBean<EmailDetectionFilter> registration = new FilterRegistrationBean<>();
		registration.setFilter(filter);
		registration.addUrlPatterns("/*"); // only APIs
		registration.setOrder(1); // run early
		return registration;
	}
}
