package com.trace.auth.service;

import com.trace.common.exception.TraceException;

public class EmailAlreadyExistsException extends TraceException {

    public EmailAlreadyExistsException(String email) {
        super(
                "EMAIL_ALREADY_EXISTS",
                "An account already exists for email: " + email
        );
    }
}