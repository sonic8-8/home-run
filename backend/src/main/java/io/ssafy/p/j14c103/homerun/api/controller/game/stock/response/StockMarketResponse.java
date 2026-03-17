package io.ssafy.p.j14c103.homerun.api.controller.game.stock.response;

import java.util.List;
import lombok.Getter;

@Getter
public class StockMarketResponse {

    private final List<StockItem> stocks;

    private StockMarketResponse(final List<StockItem> stocks) {
        this.stocks = stocks;
    }

    public static StockMarketResponse from(final List<StockItem> stocks) {
        return new StockMarketResponse(stocks);
    }

    @Getter
    public static class StockItem {
        private final String stockCode;
        private final String stockName;
        private final int currentPrice;
        private final String pricePerShare;

        public StockItem(final String stockCode, final String stockName, final int currentPrice) {
            this.stockCode = stockCode;
            this.stockName = stockName;
            this.currentPrice = currentPrice;
            this.pricePerShare = String.format("%,d 원 / 주", currentPrice);
        }
    }
}
