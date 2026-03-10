package io.ssafy.p.j14c103.homerun.domain.money;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @DisplayName("BigDecimal 값으로 Money를 생성할 수 있다")
    @Test
    void of_bigDecimal() {
        final Money money = Money.of(BigDecimal.valueOf(1000));

        assertThat(money.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(1000));
    }

    @DisplayName("long 값으로 Money를 생성할 수 있다")
    @Test
    void of_long() {
        final Money money = Money.of(1000L);

        assertThat(money.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(1000));
    }

    @DisplayName("null로 Money를 생성하면 예외가 발생한다")
    @Test
    void of_null_exception() {
        assertThatThrownBy(() -> Money.of((BigDecimal) null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("zero()는 금액이 0인 Money를 반환한다")
    @Test
    void zero() {
        final Money money = Money.zero();

        assertThat(money.isZero()).isTrue();
    }

    @DisplayName("두 Money를 더할 수 있다")
    @Test
    void add() {
        final Money a = Money.of(1000L);
        final Money b = Money.of(2000L);

        final Money result = a.add(b);

        assertThat(result).isEqualTo(Money.of(3000L));
    }

    @DisplayName("null을 더하면 예외가 발생한다")
    @Test
    void add_null_exception() {
        final Money money = Money.of(1000L);

        assertThatThrownBy(() -> money.add(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("두 Money를 뺄 수 있다")
    @Test
    void subtract() {
        final Money a = Money.of(3000L);
        final Money b = Money.of(1000L);

        final Money result = a.subtract(b);

        assertThat(result).isEqualTo(Money.of(2000L));
    }

    @DisplayName("큰 금액보다 작은 금액이면 isGreaterThan은 false를 반환한다")
    @Test
    void isGreaterThan_false() {
        final Money small = Money.of(1000L);
        final Money big = Money.of(2000L);

        assertThat(small.isGreaterThan(big)).isFalse();
    }

    @DisplayName("작은 금액보다 큰 금액이면 isGreaterThan은 true를 반환한다")
    @Test
    void isGreaterThan_true() {
        final Money big = Money.of(2000L);
        final Money small = Money.of(1000L);

        assertThat(big.isGreaterThan(small)).isTrue();
    }

    @DisplayName("음수 금액은 isNegative가 true를 반환한다")
    @Test
    void isNegative_true() {
        final Money money = Money.of(1000L).subtract(Money.of(2000L));

        assertThat(money.isNegative()).isTrue();
    }

    @DisplayName("양수 금액은 isNegative가 false를 반환한다")
    @Test
    void isNegative_false() {
        final Money money = Money.of(1000L);

        assertThat(money.isNegative()).isFalse();
    }

    @DisplayName("같은 금액의 Money는 equals가 true를 반환한다")
    @Test
    void equals_same_amount() {
        final Money a = Money.of(1000L);
        final Money b = Money.of(BigDecimal.valueOf(1000));

        assertThat(a).isEqualTo(b);
    }

    @DisplayName("다른 금액의 Money는 equals가 false를 반환한다")
    @Test
    void equals_different_amount() {
        final Money a = Money.of(1000L);
        final Money b = Money.of(2000L);

        assertThat(a).isNotEqualTo(b);
    }
}
