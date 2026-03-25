package io.ssafy.p.j14c103.homerun.domain.gamesession.stock;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StockHoldingTest {

    @DisplayName("주식 보유를 생성할 수 있다")
    @Test
    void create() {
        final StockHolding holding = StockHolding.create(1L, "SAMSUNG", 72000, 10);

        assertThat(holding.getGameSessionId()).isEqualTo(1L);
        assertThat(holding.getStockCode()).isEqualTo("SAMSUNG");
        assertThat(holding.getAveragePurchasePriceAmount()).isEqualTo(72000);
        assertThat(holding.getQuantity()).isEqualTo(10);
    }

    @DisplayName("매수 체결 시 평단가를 재계산하고 수량을 증가시킨다")
    @Test
    void addShares() {
        // 72000원에 10주 보유
        final StockHolding holding = StockHolding.create(1L, "SAMSUNG", 72000, 10);

        // 80000원에 5주 추가 매수
        holding.addShares(80000, 5);

        // 평단가: (72000*10 + 80000*5) / 15 = 1120000/15 = 74666
        assertThat(holding.getQuantity()).isEqualTo(15);
        assertThat(holding.getAveragePurchasePriceAmount()).isEqualTo(74666);
    }

    @DisplayName("매도 체결 시 수량을 감소시킨다")
    @Test
    void removeShares() {
        final StockHolding holding = StockHolding.create(1L, "SAMSUNG", 72000, 10);

        holding.removeShares(3);

        assertThat(holding.getQuantity()).isEqualTo(7);
    }

    @DisplayName("수량이 0이면 isEmpty가 true를 반환한다")
    @Test
    void isEmpty_zero() {
        final StockHolding holding = StockHolding.create(1L, "SAMSUNG", 72000, 10);

        holding.removeShares(10);

        assertThat(holding.isEmpty()).isTrue();
    }

    @DisplayName("수량이 있으면 isEmpty가 false를 반환한다")
    @Test
    void isEmpty_has_shares() {
        final StockHolding holding = StockHolding.create(1L, "SAMSUNG", 72000, 10);

        assertThat(holding.isEmpty()).isFalse();
    }
}
