package io.ssafy.p.j14c103.homerun.domain.user.auth;

import java.util.Objects;

public record AuthenticatedUser(
        Long userId,
        String email
) {

    public AuthenticatedUser {
        Objects.requireNonNull(userId, "userId는 null일 수 없습니다.");

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email은 비어 있을 수 없습니다.");
        }
    }
}
