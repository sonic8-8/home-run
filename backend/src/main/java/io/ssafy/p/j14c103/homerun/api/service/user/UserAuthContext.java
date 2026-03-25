package io.ssafy.p.j14c103.homerun.api.service.user;

import io.ssafy.p.j14c103.homerun.domain.user.User;

public record UserAuthContext(
        Long userId,
        String ssafyUserKey
) {

    public static UserAuthContext from(final User user) {
        return new UserAuthContext(user.getId(), user.getSsafyUserKey());
    }

    public boolean hasSsafyUserKey() {
        return ssafyUserKey != null && !ssafyUserKey.isBlank();
    }
}
