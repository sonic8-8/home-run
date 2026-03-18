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
 * 게임 세션별 주식 보유 현황.
 */
@Getter
@Entity
@Table(name = "stock_holdings")
@IdClass(StockHoldingId.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockHolding {

    @Id
    @Column(name = "game_session_id")
    private Integer gameSessionId;

    @Id
    @Column(name = "stock_code", length = 20)
    private String stockCode;

    @Column(name = "average_purchase_price_amount")
    private Integer averagePurchasePriceAmount;

    @Column(name = "quantity")
    private Integer quantity;

    private StockHolding(
            final Integer gameSessionId,
            final String stockCode,
            final Integer averagePurchasePriceAmount,
            final Integer quantity
    ) {
        this.gameSessionId = gameSessionId;
        this.stockCode = stockCode;
        this.averagePurchasePriceAmount = averagePurchasePriceAmount;
        this.quantity = quantity;
    }

    public static StockHolding create(
            final Integer gameSessionId,
            final String stockCode,
            final Integer purchasePrice,
            final Integer quantity
    ) {
        return new StockHolding(gameSessionId, stockCode, purchasePrice, quantity);
    }

    /**
     * 매수 체결 시 평단가를 재계산하고 수량을 증가시킨다.
     */
    public void addShares(final Integer executionPrice, final Integer addQuantity) {
        final int totalCost = this.averagePurchasePriceAmount * this.quantity
                + executionPrice * addQuantity;
        final int totalQuantity = this.quantity + addQuantity;
        this.averagePurchasePriceAmount = totalCost / totalQuantity;
        this.quantity = totalQuantity;
    }

    /**
     * 매도 체결 시 수량을 감소시킨다.
     */
    public void removeShares(final Integer removeQuantity) {
        this.quantity -= removeQuantity;
    }

    public boolean isEmpty() {
        return this.quantity == null || this.quantity <= 0;
    }
}
