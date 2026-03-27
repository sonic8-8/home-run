package io.ssafy.p.j14c103.homerun.api.service.game.stock.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StockOrderServiceRequest {

    private String stockCode;
    private String orderType;
    private Integer quantity;

    @Builder(access = AccessLevel.PRIVATE)
    private StockOrderServiceRequest(
            final String stockCode,
            final String orderType,
            final Integer quantity
    ) {
        this.stockCode = stockCode;
        this.orderType = orderType;
        this.quantity = quantity;
    }

    public static StockOrderServiceRequest of(
            final String stockCode,
            final String orderType,
            final Integer quantity
    ) {
        return StockOrderServiceRequest.builder()
                .stockCode(stockCode)
                .orderType(orderType)
                .quantity(quantity)
                .build();
    }
}
