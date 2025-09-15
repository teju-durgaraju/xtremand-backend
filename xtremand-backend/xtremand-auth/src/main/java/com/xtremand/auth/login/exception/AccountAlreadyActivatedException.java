package com.xtremand.auth.login.exception;

import org.springframework.security.core.AuthenticationException;

public class AccountAlreadyActivatedException extends AuthenticationException {
    public AccountAlreadyActivatedException(String msg) {
        super(msg);
    }
}
