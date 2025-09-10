package com.xtremand.user.service;

import com.xtremand.domain.entity.User;

public interface ActivationService {

    void createActivationTokenAndSendEmail(User user);

    void activateUser(String token);
}
