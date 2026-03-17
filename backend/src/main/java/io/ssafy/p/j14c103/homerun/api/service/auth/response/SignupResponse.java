package io.ssafy.p.j14c103.homerun.api.service.auth.response;

import io.ssafy.p.j14c103.homerun.domain.user.User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SignupResponse {

    private Long userId;
    private String email;
    private String name;

    @Builder
    private SignupResponse(Long userId, String email, String name) {
        this.userId = userId;
        this.email = email;
        this.name = name;
    }

    public static SignupResponse of(User user) {
        return SignupResponse.builder()
                .userId(user.getId())
                .email(user.getEmail().getValue())
                .name(user.getName())
                .build();
    }
}
