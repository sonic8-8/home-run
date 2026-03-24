package io.ssafy.p.j14c103.homerun.domain.gamesession.stock;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주식 매수/매도 주문.
 * 현재 턴에 등록 → 다음 턴 정산 시 체결.
 */
@Getter
@Entity
@Table(name = "stock_orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_order_id")
    private Integer stockOrderId;

    @Column(name = "game_session_id", nullable = false)
    private Long gameSessionId;

    @Column(name = "stock_code", nullable = false, length = 20)
    private String stockCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", length = 20)
    private OrderType orderType;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "ordered_turn")
    private Integer orderedTurn;

    @Column(name = "execute_turn")
    private Integer executeTurn;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", length = 20)
    private OrderStatus orderStatus;

    private StockOrder(
            final Long gameSessionId,
            final String stockCode,
            final OrderType orderType,
            final Integer quantity,
            final Integer orderedTurn
    ) {
        this.gameSessionId = gameSessionId;
        this.stockCode = stockCode;
        this.orderType = orderType;
        this.quantity = quantity;
        this.orderedTurn = orderedTurn;
        this.executeTurn = orderedTurn + 1;
        this.orderStatus = OrderStatus.PENDING;
    }

    public static StockOrder createBuyOrder(
            final Long gameSessionId,
            final String stockCode,
            final Integer quantity,
            final Integer currentTurn
    ) {
        return new StockOrder(gameSessionId, stockCode, OrderType.BUY, quantity, currentTurn);
    }

    public static StockOrder createSellOrder(
            final Long gameSessionId,
            final String stockCode,
            final Integer quantity,
            final Integer currentTurn
    ) {
        return new StockOrder(gameSessionId, stockCode, OrderType.SELL, quantity, currentTurn);
    }

    public void execute() {
        this.orderStatus = OrderStatus.EXECUTED;
    }

    public void cancel() {
        this.orderStatus = OrderStatus.CANCELED;
    }

    public boolean isPending() {
        return this.orderStatus == OrderStatus.PENDING;
    }
}
