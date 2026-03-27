package io.ssafy.p.j14c103.homerun.api.service.game.stock.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class StockOrderServiceResponse {

    private final Integer orderId;
    private final String stockCode;
    private final String orderType;
    private final int quantity;
    private final int pricePerShare;
    private final int totalAmount;
    private final String executeTurn;
    private final String orderStatus;

    @Builder(access = AccessLevel.PRIVATE)
    private StockOrderServiceResponse(
            final Integer orderId,
            final String stockCode,
            final String orderType,
            final int quantity,
            final int pricePerShare,
            final int totalAmount,
            final String executeTurn,
            final String orderStatus
    ) {
        this.orderId = orderId;
        this.stockCode = stockCode;
        this.orderType = orderType;
        this.quantity = quantity;
        this.pricePerShare = pricePerShare;
        this.totalAmount = totalAmount;
        this.executeTurn = executeTurn;
        this.orderStatus = orderStatus;
    }

    public static StockOrderServiceResponse of(
            final Integer orderId,
            final String stockCode,
            final String orderType,
            final int quantity,
            final int pricePerShare,
            final String executeTurn,
            final String orderStatus
    ) {
        return StockOrderServiceResponse.builder()
                .orderId(orderId)
                .stockCode(stockCode)
                .orderType(orderType)
                .quantity(quantity)
                .pricePerShare(pricePerShare)
                .totalAmount(pricePerShare * quantity)
                .executeTurn(executeTurn)
                .orderStatus(orderStatus)
                .build();
    }
}
