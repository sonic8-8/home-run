package io.ssafy.p.j14c103.homerun.domain.user.auth;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.support.RedisContainerTestSupport;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

@Tag("container")
@Testcontainers(disabledWithoutDocker = true)
class RefreshTokenRepositoryTest extends RedisContainerTestSupport {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @AfterEach
    void tearDown() {
        Set<String> keys = stringRedisTemplate.keys("auth:refresh:*");

        if (keys == null || keys.isEmpty()) {
            return;
        }

        stringRedisTemplate.delete(keys);
    }

    @DisplayName("Refresh Token을 Redis에 저장하고 조회할 수 있다.")
    @Test
    void saveAndFindByUserId() {
        // given
        Instant expiresAt = Instant.now()
                .plus(Duration.ofMinutes(10))
                .truncatedTo(ChronoUnit.SECONDS);
        RefreshToken refreshToken = RefreshToken.issue(
                1L,
                "refresh-token-value",
                expiresAt
        );

        // when
        refreshTokenRepository.save(refreshToken);
        RefreshToken result = refreshTokenRepository.findByUserId(1L)
                .orElseThrow();

        // then
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getTokenValue()).isEqualTo("refresh-token-value");
        assertThat(result.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(stringRedisTemplate.hasKey("auth:refresh:1")).isTrue();
        assertThat(stringRedisTemplate.getExpire("auth:refresh:1")).isPositive();
    }

    @DisplayName("같은 사용자의 Refresh Token을 다시 저장하면 기존 값을 덮어쓴다.")
    @Test
    void overwriteRefreshTokenWhenSavedAgain() {
        // given
        Instant oldExpiresAt = Instant.now()
                .plus(Duration.ofMinutes(5))
                .truncatedTo(ChronoUnit.SECONDS);
        refreshTokenRepository.save(
                RefreshToken.issue(
                        2L,
                        "old-token",
                        oldExpiresAt
                )
        );

        Instant newExpiresAt = Instant.now()
                .plus(Duration.ofMinutes(20))
                .truncatedTo(ChronoUnit.SECONDS);
        RefreshToken latestRefreshToken = RefreshToken.issue(
                2L,
                "new-token",
                newExpiresAt
        );

        // when
        refreshTokenRepository.save(latestRefreshToken);

        // then
        RefreshToken result = refreshTokenRepository.findByUserId(2L)
                .orElseThrow();

        assertThat(result.getTokenValue()).isEqualTo("new-token");
        assertThat(result.getExpiresAt()).isEqualTo(newExpiresAt);
        assertThat(stringRedisTemplate.hasKey("auth:refresh:2")).isTrue();
        assertThat(stringRedisTemplate.getExpire("auth:refresh:2")).isPositive();
    }

    @DisplayName("저장되지 않은 Refresh Token은 조회되지 않는다.")
    @Test
    void returnEmptyWhenRefreshTokenDoesNotExist() {
        // when
        boolean exists = refreshTokenRepository.findByUserId(99L).isPresent();

        // then
        assertThat(exists).isFalse();
    }
}
