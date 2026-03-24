package io.ssafy.p.j14c103.homerun.domain.gamesession.stock;

import java.io.Serializable;
import java.util.Objects;

/**
 * StockHolding 복합 키 클래스.
 */
public class StockHoldingId implements Serializable {

    private Long gameSessionId;
    private String stockCode;

    public StockHoldingId() {
    }

    public StockHoldingId(final Long gameSessionId, final String stockCode) {
        this.gameSessionId = gameSessionId;
        this.stockCode = stockCode;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final StockHoldingId that = (StockHoldingId) o;
        return Objects.equals(gameSessionId, that.gameSessionId)
                && Objects.equals(stockCode, that.stockCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameSessionId, stockCode);
    }
}
