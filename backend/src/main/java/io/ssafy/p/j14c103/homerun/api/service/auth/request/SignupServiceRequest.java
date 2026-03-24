package io.ssafy.p.j14c103.homerun.api.service.auth.request;

import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupServiceRequest {

    private String name;
    private String email;
    private String password;

    @Builder
    private SignupServiceRequest(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public User toEntity(Email email, String passwordHash) {
        return User.register(email, name, passwordHash);
    }
}
