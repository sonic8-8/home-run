package io.ssafy.p.j14c103.homerun.api.service.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MainAccountInitialBalanceServiceTest {

    private final MainAccountInitialBalanceService mainAccountInitialBalanceService =
            new MainAccountInitialBalanceService();

    @DisplayName("MAIN 초기 잔액은 300만원에서 1000만원 사이의 1만원 단위로 생성된다.")
    @Test
    void generateInitialBalanceInConfiguredRange() {
        // when
        final int balance = mainAccountInitialBalanceService.generateInitialBalance(1L);

        // then
        assertThat(balance).isBetween(3_000_000, 10_000_000);
        assertThat(balance % 10_000).isZero();
    }

    @DisplayName("MAIN 초기 잔액은 같은 유저에게 항상 같은 값으로 생성된다.")
    @Test
    void generateInitialBalanceDeterministicallyPerUser() {
        // when
        final int firstBalance = mainAccountInitialBalanceService.generateInitialBalance(7L);
        final int secondBalance = mainAccountInitialBalanceService.generateInitialBalance(7L);
        final int otherUserBalance = mainAccountInitialBalanceService.generateInitialBalance(8L);

        // then
        assertThat(firstBalance).isEqualTo(secondBalance);
        assertThat(otherUserBalance).isNotEqualTo(firstBalance);
    }

    @DisplayName("사용자 ID가 없으면 MAIN 초기 잔액을 생성할 수 없다.")
    @Test
    void generateInitialBalanceWithoutUserId() {
        assertThatThrownBy(() -> mainAccountInitialBalanceService.generateInitialBalance(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용자 ID는 필수");
    }
}
