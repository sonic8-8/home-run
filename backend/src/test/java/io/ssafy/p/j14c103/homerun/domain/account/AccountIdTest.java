package io.ssafy.p.j14c103.homerun.domain.account;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountIdTest {

    @DisplayName("문자열로 AccountId를 생성할 수 있다")
    @Test
    void of() {
        final AccountId accountId = AccountId.of("0016174648358792");

        assertThat(accountId.getValue()).isEqualTo("0016174648358792");
    }

    @DisplayName("null로 AccountId를 생성하면 예외가 발생한다")
    @Test
    void of_null_exception() {
        assertThatThrownBy(() -> AccountId.of(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("빈 문자열로 AccountId를 생성하면 예외가 발생한다")
    @Test
    void of_blank_exception() {
        assertThatThrownBy(() -> AccountId.of("  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("같은 값의 AccountId는 equals가 true를 반환한다")
    @Test
    void equals_same_value() {
        final AccountId a = AccountId.of("0016174648358792");
        final AccountId b = AccountId.of("0016174648358792");

        assertThat(a).isEqualTo(b);
    }

    @DisplayName("다른 값의 AccountId는 equals가 false를 반환한다")
    @Test
    void equals_different_value() {
        final AccountId a = AccountId.of("0016174648358792");
        final AccountId b = AccountId.of("0204667768182760");

        assertThat(a).isNotEqualTo(b);
    }
}
