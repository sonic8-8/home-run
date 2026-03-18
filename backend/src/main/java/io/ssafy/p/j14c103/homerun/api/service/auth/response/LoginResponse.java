package io.ssafy.p.j14c103.homerun.api.service.auth.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private Long accessTokenExpiresIn;

    @Builder
    private LoginResponse(String accessToken, String refreshToken, Long accessTokenExpiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpiresIn = accessTokenExpiresIn;
    }

    public static LoginResponse of(String accessToken, String refreshToken, long accessTokenExpiresIn) {
        return LoginResponse.builder().accessToken(accessToken).refreshToken(refreshToken).accessTokenExpiresIn(accessTokenExpiresIn).build();
    }
}
