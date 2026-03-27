package io.ssafy.p.j14c103.homerun.api.service.game.stock.response;

import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class StockHoldingsServiceResponse {

    private final int totalValue;
    private final double totalReturnRate;
    private final int totalPurchaseAmount;
    private final List<HoldingItemResponse> holdings;

    @Builder(access = AccessLevel.PRIVATE)
    private StockHoldingsServiceResponse(
            final int totalValue,
            final double totalReturnRate,
            final int totalPurchaseAmount,
            final List<HoldingItemResponse> holdings
    ) {
        this.totalValue = totalValue;
        this.totalReturnRate = totalReturnRate;
        this.totalPurchaseAmount = totalPurchaseAmount;
        this.holdings = holdings;
    }

    public static StockHoldingsServiceResponse of(final List<HoldingItemResponse> holdings) {
        final int totalValue = holdings.stream()
                .mapToInt(HoldingItemResponse::getCurrentValue)
                .sum();
        final int totalPurchaseAmount = holdings.stream()
                .mapToInt(item -> item.getAvgPurchasePrice() * item.getQuantity())
                .sum();
        final double totalReturnRate = totalPurchaseAmount > 0
                ? Math.round(((double) (totalValue - totalPurchaseAmount) / totalPurchaseAmount) * 1000) / 10.0
                : 0.0;

        return StockHoldingsServiceResponse.builder()
                .totalValue(totalValue)
                .totalReturnRate(totalReturnRate)
                .totalPurchaseAmount(totalPurchaseAmount)
                .holdings(holdings)
                .build();
    }

    @Getter
    public static class HoldingItemResponse {

        private final String stockCode;
        private final String stockName;
        private final int currentValue;
        private final int quantity;
        private final int avgPurchasePrice;
        private final double returnRate;

        @Builder(access = AccessLevel.PRIVATE)
        private HoldingItemResponse(
                final String stockCode,
                final String stockName,
                final int currentValue,
                final int quantity,
                final int avgPurchasePrice,
                final double returnRate
        ) {
            this.stockCode = stockCode;
            this.stockName = stockName;
            this.currentValue = currentValue;
            this.quantity = quantity;
            this.avgPurchasePrice = avgPurchasePrice;
            this.returnRate = returnRate;
        }

        public static HoldingItemResponse of(
                final String stockCode,
                final String stockName,
                final int currentPrice,
                final int quantity,
                final int avgPurchasePrice
        ) {
            final double returnRate = avgPurchasePrice > 0
                    ? Math.round(((double) (currentPrice - avgPurchasePrice) / avgPurchasePrice) * 1000) / 10.0
                    : 0.0;

            return HoldingItemResponse.builder()
                    .stockCode(stockCode)
                    .stockName(stockName)
                    .currentValue(currentPrice * quantity)
                    .quantity(quantity)
                    .avgPurchasePrice(avgPurchasePrice)
                    .returnRate(returnRate)
                    .build();
        }
    }
}
