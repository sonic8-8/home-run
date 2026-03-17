package io.ssafy.p.j14c103.homerun.domain.user.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class RedisRefreshTokenRepository implements RefreshTokenRepository {

    private static final String KEY_PREFIX = "auth:refresh:";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void save(RefreshToken refreshToken) {
        Duration ttl = Duration.between(Instant.now(), refreshToken.getExpiresAt());

        if (ttl.isZero() || ttl.isNegative()) {
            throw HomerunException.from(ErrorCode.AUTH_REFRESH_STATE_INVALID);
        }

        stringRedisTemplate.opsForValue().set(
                key(refreshToken.getUserId()),
                serialize(refreshToken),
                ttl
        );
    }

    @Override
    public Optional<RefreshToken> findByUserId(Long userId) {
        String value = stringRedisTemplate.opsForValue().get(key(userId));

        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(deserialize(value));
    }

    @Override
    public void deleteByUserId(Long userId) {
        stringRedisTemplate.delete(key(userId));
    }

    private String key(Long userId) {
        return KEY_PREFIX + userId;
    }

    private String serialize(RefreshToken refreshToken) {
        try {
            return objectMapper.writeValueAsString(new RefreshTokenCacheValue(
                    refreshToken.getUserId(),
                    refreshToken.getTokenValue(),
                    refreshToken.getExpiresAt()
            ));
        } catch (JsonProcessingException exception) {
            throw HomerunException.from(ErrorCode.GLOBAL_SERIALIZATION_ERROR, exception);
        }
    }

    private RefreshToken deserialize(String value) {
        try {
            RefreshTokenCacheValue cacheValue = objectMapper.readValue(value, RefreshTokenCacheValue.class);
            return RefreshToken.issue(
                    cacheValue.userId(),
                    cacheValue.tokenValue(),
                    cacheValue.expiresAt()
            );
        } catch (JsonProcessingException exception) {
            throw HomerunException.from(ErrorCode.GLOBAL_SERIALIZATION_ERROR, exception);
        }
    }

    private record RefreshTokenCacheValue(
            Long userId,
            String tokenValue,
            Instant expiresAt
    ) {
    }
}
