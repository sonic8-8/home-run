package io.ssafy.p.j14c103.homerun.api.service.auth.request;

import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupServiceRequest {

    private String name;
    private String email;
    private String password;
    private List<String> paymentTypes;

    @Builder
    private SignupServiceRequest(String name, String email, String password, List<String> paymentTypes) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.paymentTypes = normalizePaymentTypes(paymentTypes);
    }

    public User toEntity(Email email, String passwordHash) {
        final User user = User.register(email, name, passwordHash);
        user.updatePaymentType(String.join(",", paymentTypes));
        return user;
    }

    private List<String> normalizePaymentTypes(final List<String> paymentTypes) {
        if (paymentTypes == null) {
            return List.of();
        }
        return List.copyOf(paymentTypes);
    }
}
