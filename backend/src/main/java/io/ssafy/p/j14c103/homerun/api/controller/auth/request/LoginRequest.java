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

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Size(max = 255, message = "이메일은 255자 이하여야 합니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 32, message = "비밀번호는 8자 이상 32자 이하여야 합니다.")
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
