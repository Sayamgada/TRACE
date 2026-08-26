package com.trace.common.exception;

public class ResourceNotFoundException extends TraceException {

    public ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message);
    }
}