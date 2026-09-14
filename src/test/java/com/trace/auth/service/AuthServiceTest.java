package com.trace.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;

import com.trace.auth.dto.RefreshTokenRequest;
import com.trace.auth.dto.TokenRefreshResponse;
import com.trace.auth.refresh.RefreshToken;
import com.trace.auth.refresh.InvalidRefreshTokenException;
import com.trace.auth.refresh.RefreshTokenService;
import com.trace.auth.security.JwtService;
import com.trace.auth.security.TraceUserDetailsService;
import com.trace.user.repository.RoleRepository;
import com.trace.user.entity.User;
import com.trace.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private TraceUserDetailsService userDetailsService;

    @Mock
    private User user;

    @Mock
    private RefreshToken currentToken;

    @Mock
    private UserDetails userDetails;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                roleRepository,
                passwordEncoder,
                authenticationManager,
                jwtService,
                refreshTokenService,
                userDetailsService
        );
    }

    @Test
    void shouldRotateRefreshTokenAndGenerateNewAccessToken() {

        String oldRefreshToken = "old-refresh-token";
        String newRefreshToken = "new-refresh-token";
        String accessToken = "new-access-token";

        RefreshTokenRequest request =
                new RefreshTokenRequest(oldRefreshToken);

        when(refreshTokenService.validateToken(oldRefreshToken))
                .thenReturn(currentToken);

        when(currentToken.getUser())
                .thenReturn(user);

        when(user.getEmail())
                .thenReturn("customer@trace.local");

        when(userDetailsService.loadUserByUsername(
                "customer@trace.local"))
                .thenReturn(userDetails);

        when(jwtService.generateToken(userDetails))
                .thenReturn(accessToken);

        when(refreshTokenService.createToken(user))
                .thenReturn(newRefreshToken);

        when(jwtService.getExpirationMillis())
                .thenReturn(900000L);

        TokenRefreshResponse response =
                authService.refresh(request);

        assertThat(response.accessToken())
                .isEqualTo(accessToken);

        assertThat(response.refreshToken())
                .isEqualTo(newRefreshToken);

        assertThat(response.tokenType())
                .isEqualTo("Bearer");

        assertThat(response.expiresIn())
                .isEqualTo(900000L);

        verify(refreshTokenService)
                .validateToken(oldRefreshToken);

        verify(currentToken)
                .revoke();

        verify(userDetailsService)
                .loadUserByUsername("customer@trace.local");

        verify(jwtService)
                .generateToken(userDetails);

        verify(refreshTokenService)
                .createToken(user);
    }

    @Test
    void shouldNotCreateNewTokenBeforeValidatingCurrentToken() {

        String refreshToken = "invalid-refresh-token";

        RefreshTokenRequest request =
                new RefreshTokenRequest(refreshToken);

        when(refreshTokenService.validateToken(refreshToken))
                .thenThrow(new InvalidRefreshTokenException(
                        "Invalid refresh token"));

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> authService.refresh(request))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessage("Invalid refresh token");

        verify(refreshTokenService)
                .validateToken(refreshToken);

        verify(refreshTokenService, never())
                .createToken(any(User.class));

        verify(currentToken, never())
                .revoke();

        verify(jwtService, never())
                .generateToken(any(UserDetails.class));
    }

    @Test
    void shouldReturnNewRefreshTokenDifferentFromOldToken() {

        String oldRefreshToken = "old-refresh-token";
        String newRefreshToken = "rotated-refresh-token";

        RefreshTokenRequest request =
                new RefreshTokenRequest(oldRefreshToken);

        when(refreshTokenService.validateToken(oldRefreshToken))
                .thenReturn(currentToken);

        when(currentToken.getUser())
                .thenReturn(user);

        when(user.getEmail())
                .thenReturn("customer@trace.local");

        when(userDetailsService.loadUserByUsername(
                "customer@trace.local"))
                .thenReturn(userDetails);

        when(jwtService.generateToken(userDetails))
                .thenReturn("new-access-token");

        when(refreshTokenService.createToken(user))
                .thenReturn(newRefreshToken);

        when(jwtService.getExpirationMillis())
                .thenReturn(900000L);

        TokenRefreshResponse response =
                authService.refresh(request);

        assertThat(response.refreshToken())
                .isNotEqualTo(oldRefreshToken);

        verify(currentToken)
                .revoke();

        verify(refreshTokenService)
                .createToken(user);
    }
}

