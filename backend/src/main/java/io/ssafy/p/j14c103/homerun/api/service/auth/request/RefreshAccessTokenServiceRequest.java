package io.ssafy.p.j14c103.homerun.api.service.auth.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RefreshAccessTokenServiceRequest {

    private String refreshToken;

    @Builder
    private RefreshAccessTokenServiceRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public static RefreshAccessTokenServiceRequest of(String refreshToken) {
        return RefreshAccessTokenServiceRequest.builder()
                .refreshToken(refreshToken)
                .build();
    }
}
