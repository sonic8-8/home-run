package io.ssafy.p.j14c103.homerun.domain.user.auth;

import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public final class AuthenticatedUser {

    private final Long userId;
    private final String email;

    public AuthenticatedUser(final Long userId, final String email) {
        this.userId = Objects.requireNonNull(userId, "userId는 null일 수 없습니다.");
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email은 비어 있을 수 없습니다.");
        }
        this.email = email;
    }
}
