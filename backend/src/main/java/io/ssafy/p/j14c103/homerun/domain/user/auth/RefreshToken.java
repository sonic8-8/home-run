package io.ssafy.p.j14c103.homerun.domain.user.auth;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.time.Instant;
import lombok.Getter;

@Getter
public class RefreshToken {

    private final Long userId;
    private final String tokenValue;
    private final Instant expiresAt;

    private RefreshToken(Long userId, String tokenValue, Instant expiresAt) {
        validate(userId, tokenValue, expiresAt);

        this.userId = userId;
        this.tokenValue = tokenValue;
        this.expiresAt = expiresAt;
    }

    public static RefreshToken issue(Long userId, String tokenValue, Instant expiresAt) {
        return new RefreshToken(userId, tokenValue, expiresAt);
    }

    private void validate(Long userId, String tokenValue, Instant expiresAt) {
        if (userId == null) {
            throw HomerunException.from(ErrorCode.AUTH_REFRESH_STATE_INVALID);
        }
        if (tokenValue == null || tokenValue.isBlank()) {
            throw HomerunException.from(ErrorCode.AUTH_REFRESH_STATE_INVALID);
        }
        if (expiresAt == null) {
            throw HomerunException.from(ErrorCode.AUTH_REFRESH_STATE_INVALID);
        }
    }
}
