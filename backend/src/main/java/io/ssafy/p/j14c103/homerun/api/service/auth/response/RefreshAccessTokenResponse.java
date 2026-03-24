package io.ssafy.p.j14c103.homerun.api.service.auth.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class RefreshAccessTokenResponse {

    private String accessToken;
    private Long accessTokenExpiresIn;

    @Builder
    private RefreshAccessTokenResponse(String accessToken, Long accessTokenExpiresIn) {
        this.accessToken = accessToken;
        this.accessTokenExpiresIn = accessTokenExpiresIn;
    }

    public static RefreshAccessTokenResponse of(String accessToken, long accessTokenExpiresIn) {
        return RefreshAccessTokenResponse.builder()
                .accessToken(accessToken)
                .accessTokenExpiresIn(accessTokenExpiresIn)
                .build();
    }
}
