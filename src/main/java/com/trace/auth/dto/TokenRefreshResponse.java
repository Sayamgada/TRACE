package com.trace.auth.dto;

public record TokenRefreshResponse(

        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn

) {
}
