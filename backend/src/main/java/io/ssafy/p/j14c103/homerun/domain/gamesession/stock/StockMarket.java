package io.ssafy.p.j14c103.homerun.domain.gamesession.stock;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주식 종목 마스터 데이터.
 * 한투 OpenAPI로 실시간 시세를 조회하여 base_price_amount를 업데이트한다.
 */
@Getter
@Entity
@Table(name = "stock_markets")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockMarket {

    @Id
    @Column(name = "stock_code", length = 20)
    private String stockCode;

    @Column(name = "stock_name", nullable = false, length = 100)
    private String stockName;

    @Column(name = "kis_stock_code", length = 10)
    private String kisStockCode;

    @Column(name = "sector", length = 100)
    private String sector;

    @Column(name = "base_price_amount")
    private Integer basePriceAmount;

    @Column(name = "volatility_rate", precision = 8, scale = 4)
    private BigDecimal volatilityRate;

    private StockMarket(
            final String stockCode,
            final String stockName,
            final String kisStockCode,
            final String sector,
            final Integer basePriceAmount,
            final BigDecimal volatilityRate
    ) {
        this.stockCode = stockCode;
        this.stockName = stockName;
        this.kisStockCode = kisStockCode;
        this.sector = sector;
        this.basePriceAmount = basePriceAmount;
        this.volatilityRate = volatilityRate;
    }

    public static StockMarket create(
            final String stockCode,
            final String stockName,
            final String kisStockCode,
            final String sector,
            final Integer basePriceAmount,
            final BigDecimal volatilityRate
    ) {
        return new StockMarket(stockCode, stockName, kisStockCode, sector, basePriceAmount, volatilityRate);
    }

    /**
     * 한투 API에서 조회한 현재가로 기준가를 업데이트한다.
     */
    public void updateBasePrice(final Integer newPrice) {
        if (newPrice != null && newPrice > 0) {
            this.basePriceAmount = newPrice;
        }
    }
}
