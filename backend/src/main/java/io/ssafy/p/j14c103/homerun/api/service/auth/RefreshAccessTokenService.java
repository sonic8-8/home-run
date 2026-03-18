package io.ssafy.p.j14c103.homerun.api.service.auth;

import io.ssafy.p.j14c103.homerun.api.service.auth.request.RefreshAccessTokenServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.auth.response.RefreshAccessTokenResponse;
import io.ssafy.p.j14c103.homerun.config.JwtProperties;
import io.ssafy.p.j14c103.homerun.config.JwtTokenProvider;
import io.ssafy.p.j14c103.homerun.domain.user.auth.RefreshToken;
import io.ssafy.p.j14c103.homerun.domain.user.auth.RefreshTokenRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RefreshAccessTokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshAccessTokenResponse refresh(RefreshAccessTokenServiceRequest request) {
        String refreshToken = request.getRefreshToken();

        jwtTokenProvider.validateRefreshToken(refreshToken);
        validateRefreshTokenState(refreshToken);

        Long userId = jwtTokenProvider.getRefreshTokenUserId(refreshToken);
        String email = jwtTokenProvider.getRefreshTokenEmail(refreshToken);
        String accessToken = jwtTokenProvider.createAccessToken(userId, email);

        return RefreshAccessTokenResponse.of(
                accessToken,
                jwtProperties.getAccessTokenTtlSeconds()
        );
    }

    private void validateRefreshTokenState(String refreshTokenValue) {
        Long userId = jwtTokenProvider.getRefreshTokenUserId(refreshTokenValue);
        RefreshToken refreshToken = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> HomerunException.from(ErrorCode.AUTH_REFRESH_STATE_INVALID));

        if (refreshToken.getTokenValue().equals(refreshTokenValue)) {
            return;
        }

        throw HomerunException.from(ErrorCode.AUTH_REFRESH_STATE_INVALID);
    }
}
