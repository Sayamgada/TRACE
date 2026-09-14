package com.trace.auth.refresh;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.trace.user.entity.User;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private User user;

    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(refreshTokenRepository);
    }

    @Test
    void shouldCreateRefreshToken() {

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String token = refreshTokenService.createToken(user);

        assertThat(token)
                .isNotBlank();

        assertThat(token.length())
                .isGreaterThan(40);

        verify(refreshTokenRepository)
                .save(any(RefreshToken.class));
    }

    @Test
    void shouldCreateDifferentTokensForEachRequest() {

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String firstToken = refreshTokenService.createToken(user);

        String secondToken = refreshTokenService.createToken(user);

        assertThat(firstToken)
                .isNotEqualTo(secondToken);

        verify(refreshTokenRepository, times(2))
                .save(any(RefreshToken.class));
    }

    @Test
    void shouldValidateActiveRefreshToken() {

        String rawToken = "valid-refresh-token";

        RefreshToken refreshToken = new RefreshToken(
                user,
                "hashed-token",
                Instant.now().plus(30, ChronoUnit.DAYS));

        when(refreshTokenRepository.findByTokenHash(any(String.class)))
                .thenReturn(Optional.of(refreshToken));

        RefreshToken result = refreshTokenService.validateToken(rawToken);

        assertThat(result)
                .isSameAs(refreshToken);

        assertThat(result.isRevoked())
                .isFalse();

        assertThat(result.isExpired())
                .isFalse();
    }

    @Test
    void shouldRejectInvalidRefreshToken() {

        when(refreshTokenRepository.findByTokenHash(any(String.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.validateToken("invalid-token"))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessage("Invalid refresh token");

        verify(refreshTokenRepository)
                .findByTokenHash(any(String.class));
    }

    @Test
    void shouldRejectRevokedRefreshToken() {

        RefreshToken refreshToken = new RefreshToken(
                user,
                "hashed-token",
                Instant.now().plus(30, ChronoUnit.DAYS));

        refreshToken.revoke();

        when(refreshTokenRepository.findByTokenHash(any(String.class)))
                .thenReturn(Optional.of(refreshToken));

        assertThatThrownBy(() -> refreshTokenService.validateToken("revoked-token"))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessage("Refresh token has been revoked");
    }

    @Test
    void shouldRejectExpiredRefreshToken() {

        RefreshToken refreshToken = new RefreshToken(
                user,
                "hashed-token",
                Instant.now().minus(1, ChronoUnit.DAYS));

        when(refreshTokenRepository.findByTokenHash(any(String.class)))
                .thenReturn(Optional.of(refreshToken));

        assertThatThrownBy(() -> refreshTokenService.validateToken("expired-token"))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessage("Refresh token has expired");
    }

    @Test
    void shouldRevokeExistingRefreshToken() {

        RefreshToken refreshToken = new RefreshToken(
                user,
                "hashed-token",
                Instant.now().plus(30, ChronoUnit.DAYS));

        when(refreshTokenRepository.findByTokenHash(any(String.class)))
                .thenReturn(Optional.of(refreshToken));

        refreshTokenService.revokeToken("refresh-token");

        assertThat(refreshToken.isRevoked())
                .isTrue();

        verify(refreshTokenRepository)
                .findByTokenHash(any(String.class));
    }

    @Test
    void shouldNotFailWhenRevokingUnknownRefreshToken() {

        when(refreshTokenRepository.findByTokenHash(any(String.class)))
                .thenReturn(Optional.empty());

        refreshTokenService.revokeToken("unknown-token");

        verify(refreshTokenRepository)
                .findByTokenHash(any(String.class));
    }
}
