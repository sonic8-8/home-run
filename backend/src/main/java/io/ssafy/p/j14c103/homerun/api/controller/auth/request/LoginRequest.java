package io.ssafy.p.j14c103.homerun.api.controller.auth.request;

import io.ssafy.p.j14c103.homerun.api.service.auth.request.LoginServiceRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = "{validation.auth.login.email.notBlank}")
    @Email(message = "{validation.auth.login.email.email}")
    @Size(max = 255, message = "{validation.auth.login.email.size}")
    private String email;

    @NotBlank(message = "{validation.auth.login.password.notBlank}")
    @Size(min = 8, max = 32, message = "{validation.auth.login.password.size}")
    private String password;

    @Builder
    private LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public LoginServiceRequest toServiceRequest() {
        return LoginServiceRequest.builder()
                .email(email)
                .password(password)
                .build();
    }
}
