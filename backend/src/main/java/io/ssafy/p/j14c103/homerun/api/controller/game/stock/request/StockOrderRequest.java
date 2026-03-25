package io.ssafy.p.j14c103.homerun.api.controller.game.stock.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StockOrderRequest {

    private String stockCode;
    private String orderType;
    private Integer quantity;
}
