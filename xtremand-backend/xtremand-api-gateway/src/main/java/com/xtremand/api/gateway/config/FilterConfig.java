package com.xtremand.api.gateway.config;

import com.xtremand.api.gateway.filter.EmailDetectionFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
@RequiredArgsConstructor
public class FilterConfig {

    private final EmailDetectionFilter emailDetectionFilter;

    @Bean
    public FilterRegistrationBean<EmailDetectionFilter> emailDetectionFilterRegistration() {
        FilterRegistrationBean<EmailDetectionFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(emailDetectionFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }
}