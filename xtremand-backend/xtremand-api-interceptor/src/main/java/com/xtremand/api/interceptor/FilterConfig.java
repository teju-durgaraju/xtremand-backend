package com.xtremand.api.interceptor;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FilterConfig {

    private final EmailVerifierService emailVerifierService;

    @Bean
    public FilterRegistrationBean<EmailDetectionFilter> emailDetectionFilter() {
        FilterRegistrationBean<EmailDetectionFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new EmailDetectionFilter(emailVerifierService));
        registrationBean.addUrlPatterns("/api/*");

        return registrationBean;
    }
}