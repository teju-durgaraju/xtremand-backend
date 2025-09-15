package com.xtremand.auth.login.exception;

import org.springframework.security.core.AuthenticationException;

public class ActivationTokenExpiredException extends AuthenticationException {
    public ActivationTokenExpiredException(String msg) {
        super(msg);
    }
}
