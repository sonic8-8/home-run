package io.ssafy.p.j14c103.homerun.domain.gamesession.stock;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StockMarketTest {

    @DisplayName("StockMarket을 생성할 수 있다")
    @Test
    void create() {
        final StockMarket stock = StockMarket.create(
                "SAMSUNG", "삼성전자", "005930", "반도체/AI",
                72000, new BigDecimal("0.1500")
        );

        assertThat(stock.getStockCode()).isEqualTo("SAMSUNG");
        assertThat(stock.getStockName()).isEqualTo("삼성전자");
        assertThat(stock.getKisStockCode()).isEqualTo("005930");
        assertThat(stock.getSector()).isEqualTo("반도체/AI");
        assertThat(stock.getBasePriceAmount()).isEqualTo(72000);
        assertThat(stock.getVolatilityRate()).isEqualByComparingTo(new BigDecimal("0.15"));
    }

    @DisplayName("한투 API에서 조회한 현재가로 기준가를 업데이트한다")
    @Test
    void updateBasePrice() {
        final StockMarket stock = StockMarket.create(
                "SAMSUNG", "삼성전자", "005930", "반도체/AI",
                72000, new BigDecimal("0.15")
        );

        stock.updateBasePrice(75000);

        assertThat(stock.getBasePriceAmount()).isEqualTo(75000);
    }

    @DisplayName("null이나 0 이하 값으로 업데이트하면 기존 가격을 유지한다")
    @Test
    void updateBasePrice_invalid_keeps_old() {
        final StockMarket stock = StockMarket.create(
                "SAMSUNG", "삼성전자", "005930", "반도체/AI",
                72000, new BigDecimal("0.15")
        );

        stock.updateBasePrice(null);
        assertThat(stock.getBasePriceAmount()).isEqualTo(72000);

        stock.updateBasePrice(0);
        assertThat(stock.getBasePriceAmount()).isEqualTo(72000);

        stock.updateBasePrice(-100);
        assertThat(stock.getBasePriceAmount()).isEqualTo(72000);
    }
}
