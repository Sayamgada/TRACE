package com.trace.auth.refresh;

import com.trace.common.exception.TraceException;

public class InvalidRefreshTokenException extends TraceException {

    public InvalidRefreshTokenException(String message) {
        super("INVALID_REFRESH_TOKEN", message);
    }
}
