package io.ssafy.p.j14c103.homerun.domain.gamesession.stock;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GameStockMarketStateTest {

    @DisplayName("게임 시작 시 원본 가격으로 세션 초기 상태를 생성한다")
    @Test
    void initializeFrom() {
        final GameStockMarketState state = GameStockMarketState.initializeFrom(
                1, "SAMSUNG", 72000, 1
        );

        assertThat(state.getGameSessionId()).isEqualTo(1);
        assertThat(state.getStockCode()).isEqualTo("SAMSUNG");
        assertThat(state.getCurrentPriceAmount()).isEqualTo(72000);
        assertThat(state.getLastUpdatedTurn()).isEqualTo(1);
    }

    @DisplayName("턴 정산 시 주가를 변동시킨다")
    @Test
    void updatePrice() {
        final GameStockMarketState state = GameStockMarketState.initializeFrom(
                1, "SAMSUNG", 72000, 1
        );

        state.updatePrice(75000, 2);

        assertThat(state.getCurrentPriceAmount()).isEqualTo(75000);
        assertThat(state.getLastUpdatedTurn()).isEqualTo(2);
    }

    @DisplayName("주가 변동 시 하한선 100원이 적용된다")
    @Test
    void updatePrice_floor_100() {
        final GameStockMarketState state = GameStockMarketState.initializeFrom(
                1, "SAMSUNG", 500, 1
        );

        state.updatePrice(-50, 2);

        assertThat(state.getCurrentPriceAmount()).isEqualTo(100);
    }
}
