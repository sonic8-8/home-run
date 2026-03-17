package io.ssafy.p.j14c103.homerun.api.controller.auth.request;

import io.ssafy.p.j14c103.homerun.api.service.auth.request.SignupServiceRequest;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequest {

    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 30, message = "이름은 30자 이하여야 합니다.")
    private String name;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Size(max = 255, message = "이메일은 255자 이하여야 합니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 32, message = "비밀번호는 8자 이상 32자 이하여야 합니다.")
    private String password;

    @NotBlank(message = "비밀번호 확인은 필수입니다.")
    private String passwordConfirm;

    @NotNull(message = "약관 동의는 필수입니다.")
    private Boolean termsAgreed;

    @Builder
    private SignupRequest(String name, String email, String password, String passwordConfirm, Boolean termsAgreed) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.passwordConfirm = passwordConfirm;
        this.termsAgreed = termsAgreed;
    }

    public SignupServiceRequest toServiceRequest() {
        return SignupServiceRequest.builder()
                .name(name)
                .email(email)
                .password(password)
                .build();
    }

    @AssertTrue(message = "비밀번호 확인이 일치하지 않습니다.")
    public boolean isPasswordConfirmed() {
        if (password == null || passwordConfirm == null) {
            return true;
        }

        return password.equals(passwordConfirm);
    }

    @AssertTrue(message = "약관 동의는 필수입니다.")
    public boolean isTermsAgreed() {
        return Boolean.TRUE.equals(termsAgreed);
    }
}
