package io.ssafy.p.j14c103.homerun.api.service.auth.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginServiceRequest {

    private String email;
    private String password;

    @Builder
    private LoginServiceRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
