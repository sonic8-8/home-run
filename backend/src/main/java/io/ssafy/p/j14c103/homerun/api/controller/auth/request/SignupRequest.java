package io.ssafy.p.j14c103.homerun.api.controller.auth.request;

import io.ssafy.p.j14c103.homerun.api.service.auth.request.SignupServiceRequest;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequest {

    @NotBlank(message = "{validation.auth.signup.name.notBlank}")
    @Size(max = 30, message = "{validation.auth.signup.name.size}")
    private String name;

    @NotBlank(message = "{validation.auth.signup.email.notBlank}")
    @Email(message = "{validation.auth.signup.email.email}")
    @Size(max = 255, message = "{validation.auth.signup.email.size}")
    private String email;

    @NotBlank(message = "{validation.auth.signup.password.notBlank}")
    @Size(min = 8, max = 32, message = "{validation.auth.signup.password.size}")
    private String password;

    @NotBlank(message = "{validation.auth.signup.passwordConfirm.notBlank}")
    private String passwordConfirm;

    @NotNull(message = "{validation.auth.signup.termsAgreed.notNull}")
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

    @AssertTrue(message = "{validation.auth.signup.passwordConfirm.assertTrue}")
    public boolean isPasswordConfirmed() {
        if (password == null || passwordConfirm == null) {
            return true;
        }

        return password.equals(passwordConfirm);
    }

    @AssertTrue(message = "{validation.auth.signup.termsAgreed.assertTrue}")
    public boolean isTermsAgreed() {
        return Boolean.TRUE.equals(termsAgreed);
    }
}
