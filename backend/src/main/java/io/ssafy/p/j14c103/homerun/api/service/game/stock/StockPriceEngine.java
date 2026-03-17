package io.ssafy.p.j14c103.homerun.api.service.game.stock;

import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.GameStockMarketState;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 매 턴 정산(Phase 1)에서 호출되는 주가 변동 엔진.
 *
 * - CyclePhase가 주어지면 사이클 기저 변동 + 노이즈 적용
 * - CyclePhase가 null이면 단순 노이즈 fallback (MVP1)
 * - 변동 후 하한선 100원 적용
 */
@Slf4j
@Component
public class StockPriceEngine {

    /**
     * 세션의 모든 종목 주가를 한 턴분 변동시킨다.
     *
     * @param states       세션별 종목 가격 상태 리스트
     * @param currentPhase 현재 경제 사이클 (null이면 노이즈만 적용)
     * @param volatilities 종목별 변동성 맵 (stockCode → volatilityRate)
     * @param turn         현재 턴 번호
     */
    public void updatePrices(
            final List<GameStockMarketState> states,
            final CyclePhase currentPhase,
            final java.util.Map<String, BigDecimal> volatilities,
            final int turn
    ) {
        for (final GameStockMarketState state : states) {
            final BigDecimal volatility = volatilities.getOrDefault(
                    state.getStockCode(), new BigDecimal("0.10"));

            final int currentPrice = state.getCurrentPriceAmount();
            final double changeRate = calculateChangeRate(currentPhase, volatility);
            final int newPrice = (int) Math.round(currentPrice * (1.0 + changeRate));

            state.updatePrice(newPrice, turn);

            log.debug("주가 변동: {} {} → {} (변동률: {:.4f}%, 사이클: {})",
                    state.getStockCode(), currentPrice, state.getCurrentPriceAmount(),
                    changeRate * 100, currentPhase);
        }
    }

    /**
     * 사이클 기저 변동 + 노이즈를 합산한 변동률을 계산한다.
     */
    double calculateChangeRate(final CyclePhase phase, final BigDecimal volatility) {
        final double baseRate = getBaseRate(phase);
        final double noiseRange = volatility.doubleValue() / 200.0;
        final double noise = ThreadLocalRandom.current().nextDouble(-noiseRange, noiseRange);

        return baseRate + noise;
    }

    /**
     * 사이클 상태에 따른 기저 변동률 (%).
     * null이면 0 (노이즈만 적용).
     */
    private double getBaseRate(final CyclePhase phase) {
        if (phase == null) {
            return 0.0;
        }
        return switch (phase) {
            case BOOM -> ThreadLocalRandom.current().nextDouble(0.01, 0.03);
            case CRISIS -> ThreadLocalRandom.current().nextDouble(-0.03, -0.01);
            case RECOVERY -> ThreadLocalRandom.current().nextDouble(0.0, 0.02);
        };
    }
}
