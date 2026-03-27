package io.ssafy.p.j14c103.homerun.api.controller.auth.request;

import io.ssafy.p.j14c103.homerun.api.service.auth.request.SignupServiceRequest;
import io.ssafy.p.j14c103.homerun.domain.spending.SpendingCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
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

    @NotEmpty(message = "{validation.auth.signup.paymentTypes.notEmpty}")
    @Size(max = 3, message = "{validation.auth.signup.paymentTypes.size}")
    @Valid
    private List<String> paymentTypes;

    @Builder
    private SignupRequest(
            String name,
            String email,
            String password,
            String passwordConfirm,
            Boolean termsAgreed,
            List<String> paymentTypes
    ) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.passwordConfirm = passwordConfirm;
        this.termsAgreed = termsAgreed;
        this.paymentTypes = paymentTypes;
    }

    public SignupServiceRequest toServiceRequest() {
        return SignupServiceRequest.builder()
                .name(name)
                .email(email)
                .password(password)
                .paymentTypes(paymentTypes)
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

    @AssertTrue(message = "{validation.auth.signup.paymentTypes.unique}")
    public boolean isPaymentTypesUnique() {
        if (paymentTypes == null) {
            return true;
        }

        return paymentTypes.stream().distinct().count() == paymentTypes.size();
    }

    @AssertTrue(message = "{validation.auth.signup.paymentTypes.allowed}")
    public boolean isPaymentTypesAllowed() {
        if (paymentTypes == null) {
            return true;
        }

        return paymentTypes.stream()
                .allMatch(this::isAllowedPaymentType);
    }

    private boolean isAllowedPaymentType(final String paymentType) {
        try {
            return SpendingCategory.fromCode(paymentType).isUserSelectable();
        } catch (final IllegalArgumentException exception) {
            return false;
        }
    }
}
