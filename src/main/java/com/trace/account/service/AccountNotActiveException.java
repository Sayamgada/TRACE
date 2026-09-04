package com.trace.account.service;

import com.trace.common.exception.TraceException;

public class AccountNotActiveException extends TraceException {

    public AccountNotActiveException(String message) {
        super("ACCOUNT_NOT_ACTIVE", message);
    }
}