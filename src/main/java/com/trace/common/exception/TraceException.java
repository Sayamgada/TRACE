package com.trace.common.exception;

public abstract class TraceException extends RuntimeException {

    private final String errorCode;

    protected TraceException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}