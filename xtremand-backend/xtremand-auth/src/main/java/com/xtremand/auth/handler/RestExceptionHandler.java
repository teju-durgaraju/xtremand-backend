package com.xtremand.auth.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.xtremand.auth.login.exception.AccountDeletedException;
import com.xtremand.auth.login.exception.AccountSuspendedException;
import com.xtremand.auth.login.exception.AccountUnapprovedException;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(AccountUnapprovedException.class)
    protected ResponseEntity<Object> handleAccountUnapproved(AccountUnapprovedException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccountSuspendedException.class)
    protected ResponseEntity<Object> handleAccountSuspended(AccountSuspendedException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccountDeletedException.class)
    protected ResponseEntity<Object> handleAccountDeleted(AccountDeletedException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }
}
