package com.xtremand.auth.login.exception;

import org.springframework.security.core.AuthenticationException;

public class InvalidActivationTokenException extends AuthenticationException {
    public InvalidActivationTokenException(String msg) {
        super(msg);
    }
}
