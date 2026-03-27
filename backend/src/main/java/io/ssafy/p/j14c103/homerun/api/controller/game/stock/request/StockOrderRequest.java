package io.ssafy.p.j14c103.homerun.api.controller.game.stock.request;

import io.ssafy.p.j14c103.homerun.api.service.game.stock.request.StockOrderServiceRequest;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StockOrderRequest {

    @NotBlank(message = "{validation.stock.order.stockCode.notBlank}")
    private String stockCode;

    @NotBlank(message = "{validation.stock.order.orderType.notBlank}")
    private String orderType;

    @Positive(message = "{validation.stock.order.quantity.positive}")
    private Integer quantity;

    @Builder(access = AccessLevel.PRIVATE)
    private StockOrderRequest(
            final String stockCode,
            final String orderType,
            final Integer quantity
    ) {
        this.stockCode = stockCode;
        this.orderType = orderType;
        this.quantity = quantity;
    }

    public StockOrderServiceRequest toServiceRequest() {
        return StockOrderServiceRequest.of(stockCode, orderType, quantity);
    }

    @AssertTrue(message = "{validation.stock.order.orderType.valid}")
    public boolean isOrderTypeValid() {
        if (orderType == null || orderType.isBlank()) {
            return true;
        }

        return "BUY".equals(orderType) || "SELL".equals(orderType);
    }
}
