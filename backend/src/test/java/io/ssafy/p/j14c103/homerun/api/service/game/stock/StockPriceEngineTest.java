package io.ssafy.p.j14c103.homerun.api.service.game.stock;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.GameStockMarketState;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class StockPriceEngineTest {

    private final StockPriceEngine engine = new StockPriceEngine();

    @DisplayName("BOOM 사이클에서 기저 변동률은 양수이다")
    @RepeatedTest(10)
    void calculateChangeRate_boom_positive() {
        final double rate = engine.calculateChangeRate(CyclePhase.BOOM, new BigDecimal("0.10"));

        // BOOM 기저: +1~3%, 노이즈 범위 ±0.05% → 최소 약 +0.95%
        assertThat(rate).isGreaterThan(0.005);
    }

    @DisplayName("CRISIS 사이클에서 기저 변동률은 음수이다")
    @RepeatedTest(10)
    void calculateChangeRate_crisis_negative() {
        final double rate = engine.calculateChangeRate(CyclePhase.CRISIS, new BigDecimal("0.10"));

        // CRISIS 기저: -3~-1%, 노이즈 범위 ±0.05% → 최대 약 -0.95%
        assertThat(rate).isLessThan(-0.005);
    }

    @DisplayName("RECOVERY 사이클에서 기저 변동률은 0 이상이다")
    @RepeatedTest(10)
    void calculateChangeRate_recovery() {
        final double rate = engine.calculateChangeRate(CyclePhase.RECOVERY, new BigDecimal("0.10"));

        // RECOVERY 기저: 0~2%, 노이즈 범위 ±0.05% → 최소 약 -0.05%
        assertThat(rate).isGreaterThan(-0.01);
    }

    @DisplayName("CyclePhase가 null이면 노이즈만 적용된다")
    @RepeatedTest(10)
    void calculateChangeRate_null_phase() {
        final double rate = engine.calculateChangeRate(null, new BigDecimal("0.10"));

        // 기저: 0, 노이즈 범위 ±0.05%
        assertThat(rate).isBetween(-0.001, 0.001);
    }

    @DisplayName("주가 변동 후 하한선 100원이 보장된다")
    @Test
    void updatePrices_floor_100() {
        final GameStockMarketState state = GameStockMarketState.initializeFrom(1, "TEST", 150, 1);

        // CRISIS로 강하게 떨어뜨려 하한선 테스트
        // 직접 updatePrice를 호출해서 하한선만 확인
        state.updatePrice(-50, 2);

        assertThat(state.getCurrentPriceAmount()).isEqualTo(100);
    }

    @DisplayName("updatePrices로 전체 종목의 가격이 변동된다")
    @Test
    void updatePrices_all_stocks() {
        final GameStockMarketState samsung = GameStockMarketState.initializeFrom(1, "SAMSUNG", 72000, 1);
        final GameStockMarketState kakao = GameStockMarketState.initializeFrom(1, "KAKAO", 55000, 1);

        final Map<String, BigDecimal> volatilities = Map.of(
                "SAMSUNG", new BigDecimal("0.15"),
                "KAKAO", new BigDecimal("0.20")
        );

        engine.updatePrices(List.of(samsung, kakao), CyclePhase.BOOM, volatilities, 2);

        assertThat(samsung.getLastUpdatedTurn()).isEqualTo(2);
        assertThat(kakao.getLastUpdatedTurn()).isEqualTo(2);
        // BOOM이므로 가격 상승하는 경향 (완전 보장은 아님 - 노이즈 존재)
        assertThat(samsung.getCurrentPriceAmount()).isNotEqualTo(72000);
        assertThat(kakao.getCurrentPriceAmount()).isNotEqualTo(55000);
    }
}
