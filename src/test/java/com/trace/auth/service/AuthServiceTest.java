package com.trace.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.trace.audit.entity.AuditAction;
import com.trace.audit.service.AuditLogService;
import com.trace.auth.dto.LoginRequest;
import com.trace.auth.dto.LoginResponse;
import com.trace.auth.dto.RefreshTokenRequest;
import com.trace.auth.dto.TokenRefreshResponse;
import com.trace.auth.refresh.InvalidRefreshTokenException;
import com.trace.auth.refresh.RefreshToken;
import com.trace.auth.refresh.RefreshTokenService;
import com.trace.auth.security.JwtService;
import com.trace.auth.security.TraceUserDetailsService;
import com.trace.user.entity.User;
import com.trace.user.repository.RoleRepository;
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
        private AuditLogService auditLogService;

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
                                userDetailsService,
                                auditLogService);
        }

        @Test
        void shouldAuditSuccessfulLogin() {

                String email = "customer@trace.local";
                String password = "password";
                String accessToken = "access-token";
                String refreshToken = "refresh-token";

                LoginRequest request = new LoginRequest(email, password);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null);

                when(authenticationManager.authenticate(any(
                                UsernamePasswordAuthenticationToken.class)))
                                .thenReturn(authentication);

                when(jwtService.generateToken(userDetails))
                                .thenReturn(accessToken);

                when(userRepository.findByEmailIgnoreCase(email))
                                .thenReturn(java.util.Optional.of(user));

                when(user.getId())
                                .thenReturn(42L);

                when(refreshTokenService.createToken(user))
                                .thenReturn(refreshToken);

                when(jwtService.getExpirationMillis())
                                .thenReturn(900000L);

                LoginResponse response = authService.login(request);

                assertThat(response.accessToken())
                                .isEqualTo(accessToken);

                assertThat(response.refreshToken())
                                .isEqualTo(refreshToken);

                assertThat(response.tokenType())
                                .isEqualTo("Bearer");

                assertThat(response.expiresIn())
                                .isEqualTo(900000L);

                verify(auditLogService).record(
                                42L,
                                AuditAction.USER_LOGIN,
                                "User",
                                42L,
                                null,
                                "LOGIN_SUCCESS",
                                null);
        }

        @Test
        void shouldNotAuditFailedLogin() {

                String email = "customer@trace.local";
                String password = "wrong-password";

                LoginRequest request = new LoginRequest(email, password);

                when(authenticationManager.authenticate(any(
                                UsernamePasswordAuthenticationToken.class)))
                                .thenThrow(new RuntimeException("Authentication failed"));

                org.assertj.core.api.Assertions.assertThatThrownBy(
                                () -> authService.login(request))
                                .isInstanceOf(RuntimeException.class)
                                .hasMessage("Authentication failed");

                verify(auditLogService, never()).record(
                                any(),
                                eq(AuditAction.USER_LOGIN),
                                any(),
                                any(),
                                any(),
                                any(),
                                any());

                verify(userRepository, never())
                                .findByEmailIgnoreCase(anyString());

                verify(refreshTokenService, never())
                                .createToken(any(User.class));
        }

        @Test
        void shouldRotateRefreshTokenAndGenerateNewAccessToken() {

                String oldRefreshToken = "old-refresh-token";
                String newRefreshToken = "new-refresh-token";
                String accessToken = "new-access-token";

                RefreshTokenRequest request = new RefreshTokenRequest(oldRefreshToken);

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

                TokenRefreshResponse response = authService.refresh(request);

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

                RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);

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

                RefreshTokenRequest request = new RefreshTokenRequest(oldRefreshToken);

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

                TokenRefreshResponse response = authService.refresh(request);

                assertThat(response.refreshToken())
                                .isNotEqualTo(oldRefreshToken);

                verify(currentToken)
                                .revoke();

                verify(refreshTokenService)
                                .createToken(user);
        }
}