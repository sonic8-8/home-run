package io.ssafy.p.j14c103.homerun.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private static final String SECRET = "01234567890123456789012345678901";

    @DisplayName("Access Token을 생성하고 claim을 조회할 수 있다.")
    @Test
    void createAndReadAccessToken() {
        // given
        Instant now = Instant.parse("2026-03-17T00:00:00Z");
        JwtTokenProvider jwtTokenProvider = jwtTokenProvider(now, SECRET);

        // when
        String accessToken = jwtTokenProvider.createAccessToken(1L, "tester@ssafy.com");

        // then
        assertThat(jwtTokenProvider.isValid(accessToken)).isTrue();
        assertThat(jwtTokenProvider.getUserId(accessToken)).isEqualTo(1L);
        assertThat(jwtTokenProvider.getEmail(accessToken)).isEqualTo("tester@ssafy.com");
        assertThat(jwtTokenProvider.getTokenType(accessToken)).isEqualTo("access");
        assertThat(jwtTokenProvider.getExpiresAt(accessToken))
                .isEqualTo(now.plusSeconds(1800L));
    }

    @DisplayName("Refresh Token을 생성하고 refresh 타입으로 구분할 수 있다.")
    @Test
    void createAndReadRefreshToken() {
        // given
        Instant now = Instant.parse("2026-03-17T00:00:00Z");
        JwtTokenProvider jwtTokenProvider = jwtTokenProvider(now, SECRET);

        // when
        String refreshToken = jwtTokenProvider.createRefreshToken(7L, "refresh@ssafy.com");

        // then
        assertThat(jwtTokenProvider.isValid(refreshToken)).isTrue();
        jwtTokenProvider.validateRefreshToken(refreshToken);
        assertThat(jwtTokenProvider.getRefreshTokenUserId(refreshToken)).isEqualTo(7L);
        assertThat(jwtTokenProvider.getRefreshTokenEmail(refreshToken))
                .isEqualTo("refresh@ssafy.com");
        assertThat(jwtTokenProvider.getTokenType(refreshToken)).isEqualTo("refresh");
        assertThat(jwtTokenProvider.getExpiresAt(refreshToken))
                .isEqualTo(now.plusSeconds(1209600L));
    }

    @DisplayName("잘못된 Refresh Token이면 AUTH_REFRESH_INVALID 예외가 발생한다.")
    @Test
    void invalidRefreshToken() {
        // given
        Instant now = Instant.parse("2026-03-17T00:00:00Z");
        JwtTokenProvider issuer = jwtTokenProvider(now, SECRET);
        JwtTokenProvider validator = jwtTokenProvider(
                now,
                "abcdefghijklmnopqrstuvwxyz123456"
        );
        String refreshToken = issuer.createRefreshToken(1L, "tester@ssafy.com");

        // when & then
        assertThatThrownBy(() -> validator.validateRefreshToken(refreshToken))
                .isInstanceOf(HomerunException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_REFRESH_INVALID);
    }

    @DisplayName("만료된 Refresh Token이면 AUTH_REFRESH_EXPIRED 예외가 발생한다.")
    @Test
    void expiredRefreshToken() {
        // given
        Instant issuedAt = Instant.parse("2026-03-17T00:00:00Z");
        String refreshToken = jwtTokenProvider(issuedAt, SECRET)
                .createRefreshToken(1L, "tester@ssafy.com");
        JwtTokenProvider validator = jwtTokenProvider(
                issuedAt.plusSeconds(1209601L),
                SECRET
        );

        // when & then
        assertThatThrownBy(() -> validator.validateRefreshToken(refreshToken))
                .isInstanceOf(HomerunException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_REFRESH_EXPIRED);
    }

    @DisplayName("Access Token으로 Refresh 검증을 시도하면 AUTH_REFRESH_INVALID 예외가 발생한다.")
    @Test
    void invalidWhenAccessTokenPassedToRefreshValidation() {
        // given
        Instant now = Instant.parse("2026-03-17T00:00:00Z");
        JwtTokenProvider jwtTokenProvider = jwtTokenProvider(now, SECRET);
        String accessToken = jwtTokenProvider.createAccessToken(1L, "tester@ssafy.com");

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.validateRefreshToken(accessToken))
                .isInstanceOf(HomerunException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_REFRESH_INVALID);
    }

    private JwtTokenProvider jwtTokenProvider(Instant now, String secret) {
        JwtProperties jwtProperties = JwtProperties.of(secret, 1800L, 1209600L);

        return JwtTokenProvider.forTest(
                jwtProperties,
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }
}
