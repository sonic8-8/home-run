package io.ssafy.p.j14c103.homerun.api.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.ssafy.p.j14c103.homerun.api.service.auth.request.RefreshAccessTokenServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.auth.response.RefreshAccessTokenResponse;
import io.ssafy.p.j14c103.homerun.config.JwtProperties;
import io.ssafy.p.j14c103.homerun.config.JwtTokenProvider;
import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.domain.user.auth.RefreshToken;
import io.ssafy.p.j14c103.homerun.domain.user.auth.RefreshTokenRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import io.ssafy.p.j14c103.homerun.support.IntegrationTestSupport;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Tag("container")
@Testcontainers(disabledWithoutDocker = true)
class RefreshAccessTokenServiceTest extends IntegrationTestSupport {

    @Container
    private static final GenericContainer<?> REDIS_CONTAINER = new GenericContainer<>(
            DockerImageName.parse("redis:7.2-alpine")
    ).withExposedPorts(6379);

    @Autowired
    private RefreshAccessTokenService refreshAccessTokenService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @DynamicPropertySource
    static void overrideRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", REDIS_CONTAINER::getFirstMappedPort);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAllInBatch();

        Set<String> keys = stringRedisTemplate.keys("auth:refresh:*");

        if (keys == null || keys.isEmpty()) {
            return;
        }

        stringRedisTemplate.delete(keys);
    }

    @DisplayName("유효한 Refresh Token이면 새 Access Token을 재발급한다.")
    @Test
    void refreshAccessToken() {
        // given
        User savedUser = saveUser("user@example.com");
        String refreshToken = jwtTokenProvider.createRefreshToken(
                savedUser.getId(),
                savedUser.getEmail().getValue()
        );

        refreshTokenRepository.save(RefreshToken.issue(
                savedUser.getId(),
                refreshToken,
                jwtTokenProvider.getExpiresAt(refreshToken)
        ));

        // when
        RefreshAccessTokenResponse response = refreshAccessTokenService.refresh(
                RefreshAccessTokenServiceRequest.of(refreshToken)
        );

        // then
        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getAccessTokenExpiresIn()).isEqualTo(jwtProperties.getAccessTokenTtlSeconds());
        assertThat(jwtTokenProvider.getUserId(response.getAccessToken())).isEqualTo(savedUser.getId());
        assertThat(jwtTokenProvider.getEmail(response.getAccessToken())).isEqualTo("user@example.com");
        assertThat(jwtTokenProvider.getTokenType(response.getAccessToken())).isEqualTo("access");
    }

    @DisplayName("유효하지 않은 Refresh Token이면 AUTH_REFRESH_INVALID 예외가 발생한다.")
    @Test
    void refreshAccessTokenWithInvalidToken() {
        // given
        String refreshToken = "invalid-refresh-token";

        // when
        Throwable thrown = catchThrowable(() -> refreshAccessTokenService.refresh(
                RefreshAccessTokenServiceRequest.of(refreshToken)
        ));

        // then
        assertThat(thrown)
                .isInstanceOf(HomerunException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_REFRESH_INVALID);
    }

    @DisplayName("만료된 Refresh Token이면 AUTH_REFRESH_EXPIRED 예외가 발생한다.")
    @Test
    void refreshAccessTokenWithExpiredToken() {
        // given
        String expiredRefreshToken = createExpiredRefreshToken(1L, Email.of("user@example.com"));

        // when
        Throwable thrown = catchThrowable(() -> refreshAccessTokenService.refresh(
                RefreshAccessTokenServiceRequest.of(expiredRefreshToken)
        ));

        // then
        assertThat(thrown)
                .isInstanceOf(HomerunException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_REFRESH_EXPIRED);
    }

    @DisplayName("Redis에 저장된 Refresh Token과 다르면 AUTH_REFRESH_STATE_INVALID 예외가 발생한다.")
    @Test
    void refreshAccessTokenWhenRefreshTokenStateDoesNotMatch() {
        // given
        User savedUser = saveUser("user@example.com");
        String savedRefreshToken = createRefreshToken(
                savedUser.getId(),
                savedUser.getEmail(),
                Instant.now().minusSeconds(10)
        );
        String requestedRefreshToken = createRefreshToken(
                savedUser.getId(),
                savedUser.getEmail(),
                Instant.now()
        );

        refreshTokenRepository.save(RefreshToken.issue(
                savedUser.getId(),
                savedRefreshToken,
                jwtTokenProvider.getExpiresAt(savedRefreshToken)
        ));

        // when
        Throwable thrown = catchThrowable(() -> refreshAccessTokenService.refresh(
                RefreshAccessTokenServiceRequest.of(requestedRefreshToken)
        ));

        // then
        assertThat(thrown)
                .isInstanceOf(HomerunException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_REFRESH_STATE_INVALID);
    }

    private User saveUser(String email) {
        return userRepository.saveAndFlush(User.register(
                Email.of(email),
                "홍길동",
                passwordEncoder.encode("Password123!")
        ));
    }

    private String createExpiredRefreshToken(Long userId, Email email) {
        return createRefreshToken(userId, email, Instant.now().minusSeconds(3600), 60L);
    }

    private String createRefreshToken(Long userId, Email email, Instant issuedAt) {
        return createRefreshToken(
                userId,
                email,
                issuedAt,
                jwtProperties.getRefreshTokenTtlSeconds()
        );
    }

    private String createRefreshToken(
            Long userId,
            Email email,
            Instant issuedAt,
            long ttlSeconds
    ) {
        Instant expiresAt = issuedAt.plusSeconds(ttlSeconds);
        SecretKey secretKey = Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email.getValue())
                .claim("tokenType", "refresh")
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }
}
