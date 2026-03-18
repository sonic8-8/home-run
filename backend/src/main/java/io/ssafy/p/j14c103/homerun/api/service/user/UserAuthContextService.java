package io.ssafy.p.j14c103.homerun.api.service.user;

import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAuthContextService {

    private final UserRepository userRepository;

    public UserAuthContext getContext(final Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }

        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new HomerunException(ErrorCode.USER_NOT_FOUND));

        return UserAuthContext.from(user);
    }

    public String getRequiredSsafyUserKey(final Long userId) {
        final UserAuthContext context = getContext(userId);
        if (context.hasSsafyUserKey()) {
            return context.ssafyUserKey();
        }

        throw new HomerunException(ErrorCode.USER_SSAFY_CONNECTION_REQUIRED);
    }
}
