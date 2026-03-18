package io.ssafy.p.j14c103.homerun.api.controller.game.stock.response;

import java.util.List;
import lombok.Getter;

@Getter
public class StockHoldingsResponse {

    private final int totalValue;
    private final double totalReturnRate;
    private final int totalPurchaseAmount;
    private final List<HoldingItem> holdings;

    private StockHoldingsResponse(
            final int totalValue,
            final double totalReturnRate,
            final int totalPurchaseAmount,
            final List<HoldingItem> holdings
    ) {
        this.totalValue = totalValue;
        this.totalReturnRate = totalReturnRate;
        this.totalPurchaseAmount = totalPurchaseAmount;
        this.holdings = holdings;
    }

    public static StockHoldingsResponse from(final List<HoldingItem> holdings) {
        final int totalValue = holdings.stream().mapToInt(HoldingItem::getCurrentValue).sum();
        final int totalPurchase = holdings.stream().mapToInt(h -> h.getAvgPurchasePrice() * h.getQuantity()).sum();
        final double totalReturn = totalPurchase > 0
                ? ((double) (totalValue - totalPurchase) / totalPurchase) * 100
                : 0.0;
        return new StockHoldingsResponse(totalValue, Math.round(totalReturn * 10.0) / 10.0, totalPurchase, holdings);
    }

    @Getter
    public static class HoldingItem {
        private final String stockCode;
        private final String stockName;
        private final int currentValue;
        private final int quantity;
        private final int avgPurchasePrice;
        private final double returnRate;

        public HoldingItem(
                final String stockCode,
                final String stockName,
                final int currentPrice,
                final int quantity,
                final int avgPurchasePrice
        ) {
            this.stockCode = stockCode;
            this.stockName = stockName;
            this.currentValue = currentPrice * quantity;
            this.quantity = quantity;
            this.avgPurchasePrice = avgPurchasePrice;
            this.returnRate = avgPurchasePrice > 0
                    ? Math.round(((double) (currentPrice - avgPurchasePrice) / avgPurchasePrice) * 1000) / 10.0
                    : 0.0;
        }
    }
}
