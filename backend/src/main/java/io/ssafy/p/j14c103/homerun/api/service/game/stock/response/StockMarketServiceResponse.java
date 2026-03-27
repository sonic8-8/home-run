package io.ssafy.p.j14c103.homerun.api.service.game.stock.response;

import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class StockMarketServiceResponse {

    private final List<StockItemResponse> stocks;

    @Builder(access = AccessLevel.PRIVATE)
    private StockMarketServiceResponse(final List<StockItemResponse> stocks) {
        this.stocks = stocks;
    }

    public static StockMarketServiceResponse of(final List<StockItemResponse> stocks) {
        return StockMarketServiceResponse.builder()
                .stocks(stocks)
                .build();
    }

    @Getter
    public static class StockItemResponse {

        private final String stockCode;
        private final String stockName;
        private final int currentPrice;
        private final String pricePerShare;

        @Builder(access = AccessLevel.PRIVATE)
        private StockItemResponse(
                final String stockCode,
                final String stockName,
                final int currentPrice,
                final String pricePerShare
        ) {
            this.stockCode = stockCode;
            this.stockName = stockName;
            this.currentPrice = currentPrice;
            this.pricePerShare = pricePerShare;
        }

        public static StockItemResponse of(
                final String stockCode,
                final String stockName,
                final int currentPrice
        ) {
            return StockItemResponse.builder()
                    .stockCode(stockCode)
                    .stockName(stockName)
                    .currentPrice(currentPrice)
                    .pricePerShare(String.format("%,d 원 / 주", currentPrice))
                    .build();
        }
    }
}
