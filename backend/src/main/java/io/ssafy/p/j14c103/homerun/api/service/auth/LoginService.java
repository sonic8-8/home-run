package io.ssafy.p.j14c103.homerun.api.service.auth;

import io.ssafy.p.j14c103.homerun.api.service.auth.request.LoginServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.auth.response.LoginResponse;
import io.ssafy.p.j14c103.homerun.config.JwtProperties;
import io.ssafy.p.j14c103.homerun.config.JwtTokenProvider;
import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.domain.user.auth.RefreshToken;
import io.ssafy.p.j14c103.homerun.domain.user.auth.RefreshTokenRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;

    public LoginResponse login(LoginServiceRequest request) {
        User user = getUser(request);

        validatePassword(request, user);

        String email = user.getEmail().getValue();
        Long userId = user.getId();
        String accessToken = jwtTokenProvider.createAccessToken(userId, email);
        String refreshTokenValue = jwtTokenProvider.createRefreshToken(userId, email);

        refreshTokenRepository.save(RefreshToken.issue(
                userId,
                refreshTokenValue,
                jwtTokenProvider.getExpiresAt(refreshTokenValue)
        ));

        return LoginResponse.of(
                accessToken,
                refreshTokenValue,
                jwtProperties.getAccessTokenTtlSeconds()
        );
    }

    private User getUser(LoginServiceRequest request) {
        return userRepository.findByEmail(Email.of(request.getEmail()))
                .orElseThrow(() -> HomerunException.from(ErrorCode.AUTH_LOGIN_FAILED));
    }

    private void validatePassword(LoginServiceRequest request, User user) {
        if (passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return;
        }

        throw HomerunException.from(ErrorCode.AUTH_LOGIN_FAILED);
    }
}
