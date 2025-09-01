package com.xtremand.email.service;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailRouteBuilder extends RouteBuilder {

    @Value("${smtp.host}")
    private String smtpHost;

    @Value("${smtp.port}")
    private String smtpPort;

    @Value("${email.username}")
    private String username;

    @Value("${email.password}")
    private String password;

    @Override
    public void configure() throws Exception {
        String smtpEndpoint = String.format("smtps://%s:%s?username=%s&password=%s&mail.smtp.auth=true&mail.smtp.starttls.enable=true",
                smtpHost, smtpPort, username, password);

        from("direct:sendEmail")
            .log("Sending email to ${header.To} with subject ${header.Subject}")
            .to(smtpEndpoint);
    }
}