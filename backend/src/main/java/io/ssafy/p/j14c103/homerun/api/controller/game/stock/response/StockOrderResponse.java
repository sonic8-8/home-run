package io.ssafy.p.j14c103.homerun.api.controller.game.stock.response;

import lombok.Getter;

@Getter
public class StockOrderResponse {

    private final Integer orderId;
    private final String stockCode;
    private final String orderType;
    private final int quantity;
    private final int pricePerShare;
    private final int totalAmount;
    private final String executeTurn;
    private final String orderStatus;

    public StockOrderResponse(
            final Integer orderId,
            final String stockCode,
            final String orderType,
            final int quantity,
            final int pricePerShare,
            final String executeTurn,
            final String orderStatus
    ) {
        this.orderId = orderId;
        this.stockCode = stockCode;
        this.orderType = orderType;
        this.quantity = quantity;
        this.pricePerShare = pricePerShare;
        this.totalAmount = pricePerShare * quantity;
        this.executeTurn = executeTurn;
        this.orderStatus = orderStatus;
    }
}
