package io.ssafy.p.j14c103.homerun.domain.gamesession.stock;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게임 세션별 주식 현재가 상태.
 * 게임 시작 시 stock_markets의 base_price를 복사해오고,
 * 이후 매 턴 정산에서 CyclePhase 기반으로 변동된다.
 */
@Getter
@Entity
@Table(name = "game_stock_market_states")
@IdClass(GameStockMarketStateId.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameStockMarketState {

    @Id
    @Column(name = "game_session_id")
    private Long gameSessionId;

    @Id
    @Column(name = "stock_code", length = 20)
    private String stockCode;

    @Column(name = "current_price_amount", nullable = false)
    private Integer currentPriceAmount;

    @Column(name = "last_updated_turn", nullable = false)
    private Integer lastUpdatedTurn;

    private GameStockMarketState(
            final Long gameSessionId,
            final String stockCode,
            final Integer currentPriceAmount,
            final Integer lastUpdatedTurn
    ) {
        this.gameSessionId = gameSessionId;
        this.stockCode = stockCode;
        this.currentPriceAmount = currentPriceAmount;
        this.lastUpdatedTurn = lastUpdatedTurn;
    }

    /**
     * 게임 시작 시 원본 가격을 복사하여 세션 초기 상태를 생성한다.
     */
    public static GameStockMarketState initializeFrom(
            final Long gameSessionId,
            final String stockCode,
            final Integer basePrice,
            final Integer startTurn
    ) {
        return new GameStockMarketState(gameSessionId, stockCode, basePrice, startTurn);
    }

    /**
     * 턴 정산 시 주가를 변동시킨다.
     */
    public void updatePrice(final Integer newPrice, final Integer turn) {
        this.currentPriceAmount = Math.max(newPrice, 100); // 하한선 100원
        this.lastUpdatedTurn = turn;
    }
}
