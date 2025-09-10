package com.xtremand.auth.forgotpassword.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.apache.camel.ProducerTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class ForgotPasswordEmailService {

    @Value("${email.username}")
    private String fromEmail;

    private final ProducerTemplate producerTemplate;

    public ForgotPasswordEmailService(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }

    public void sendForgotPasswordEmail(String toEmail, String resetLink) {
        String subject = "Reset Your Password";
        String body = "Please click the following link to reset your password: " + resetLink;

        Map<String, Object> headers = new HashMap<>();
        headers.put("To", toEmail);
        headers.put("From", fromEmail);
        headers.put("Subject", subject);
        headers.put("Content-Type", "text/html; charset=UTF-8");

        producerTemplate.sendBodyAndHeaders("direct:sendEmail", body, headers);
    }
}
