package com.xtremand.email.notification;

public interface NotificationService {

    void sendActivationEmail(String to, String activationLink);
}
