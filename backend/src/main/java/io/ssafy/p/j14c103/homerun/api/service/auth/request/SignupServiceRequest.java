package io.ssafy.p.j14c103.homerun.api.service.auth.request;

import io.ssafy.p.j14c103.homerun.domain.spending.SpendingCategory;
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
        validatePaymentTypes(paymentTypes);
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

    private void validatePaymentTypes(final List<String> paymentTypes) {
        if (paymentTypes == null || paymentTypes.isEmpty()) {
            throw new IllegalArgumentException("소비 선호 카테고리는 최소 1개 이상 선택해야 합니다.");
        }
        if (paymentTypes.size() > 3) {
            throw new IllegalArgumentException("소비 선호 카테고리는 최대 3개까지 선택할 수 있습니다.");
        }
        if (paymentTypes.stream().distinct().count() != paymentTypes.size()) {
            throw new IllegalArgumentException("소비 선호 카테고리는 중복 선택할 수 없습니다.");
        }
        final boolean hasInvalidCode = paymentTypes.stream()
                .map(this::toSpendingCategory)
                .anyMatch(category -> !category.isUserSelectable());
        if (hasInvalidCode) {
            throw new IllegalArgumentException("허용되지 않은 소비 선호 카테고리입니다.");
        }
    }

    private SpendingCategory toSpendingCategory(final String paymentType) {
        return SpendingCategory.fromCode(paymentType);
    }
}
