package com.trace.auth.dto;

public record RegistrationResponse(
        Long userId,
        String name,
        String email,
        String message
) {
}