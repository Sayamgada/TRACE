package com.trace.auth.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.trace.auth.dto.TokenRefreshResponse;
import com.trace.auth.refresh.InvalidRefreshTokenException;
import com.trace.auth.service.AuthService;
import com.trace.auth.dto.RefreshTokenRequest;
import com.trace.common.exception.GlobalExceptionHandler;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockMvc = standaloneSetup(
                new AuthController(authService)
        )
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldRefreshTokenSuccessfully() throws Exception {

        TokenRefreshResponse response =
                new TokenRefreshResponse(
                        "new-access-token",
                        "new-refresh-token",
                        "Bearer",
                        900000L
                );

        when(authService.refresh(any(RefreshTokenRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "refreshToken": "old-refresh-token"
                                }
                                """)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken")
                        .value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken")
                        .value("new-refresh-token"))
                .andExpect(jsonPath("$.tokenType")
                        .value("Bearer"))
                .andExpect(jsonPath("$.expiresIn")
                        .value(900000));

        verify(authService)
                .refresh(any(RefreshTokenRequest.class));
    }

    @Test
    void shouldRejectMissingRefreshToken() throws Exception {

        mockMvc.perform(
                post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "refreshToken": ""
                                }
                                """)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message")
                        .value("refreshToken: Refresh token is required"))
                .andExpect(jsonPath("$.path")
                        .value("/api/auth/refresh"));

        verifyNoInteractions(authService);
    }

    @Test
    void shouldRejectInvalidRefreshToken() throws Exception {

        when(authService.refresh(any(RefreshTokenRequest.class)))
                .thenThrow(
                        new InvalidRefreshTokenException(
                                "Invalid refresh token"
                        )
                );

        mockMvc.perform(
                post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "refreshToken": "invalid-token"
                                }
                                """)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error")
                        .value("INVALID_REFRESH_TOKEN"))
                .andExpect(jsonPath("$.message")
                        .value("Invalid refresh token"))
                .andExpect(jsonPath("$.path")
                        .value("/api/auth/refresh"));
    }

    @Test
    void shouldRejectBlankRefreshToken() throws Exception {

        mockMvc.perform(
                post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "refreshToken": "   "
                                }
                                """)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message")
                        .value("refreshToken: Refresh token is required"));

        verifyNoInteractions(authService);
    }
}
