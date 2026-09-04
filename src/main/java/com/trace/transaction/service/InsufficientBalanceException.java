package com.trace.transaction.service;

import com.trace.common.exception.TraceException;

public class InsufficientBalanceException extends TraceException {

    public InsufficientBalanceException(String message) {
        super("INSUFFICIENT_BALANCE", message);
    }
}