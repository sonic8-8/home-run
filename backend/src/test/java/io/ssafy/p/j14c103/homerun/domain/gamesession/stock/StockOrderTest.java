package io.ssafy.p.j14c103.homerun.domain.gamesession.stock;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StockOrderTest {

    @DisplayName("매수 주문을 생성하면 PENDING 상태이고 다음 턴에 체결된다")
    @Test
    void createBuyOrder() {
        final StockOrder order = StockOrder.createBuyOrder(1, "SAMSUNG", 5, 3);

        assertThat(order.getGameSessionId()).isEqualTo(1);
        assertThat(order.getStockCode()).isEqualTo("SAMSUNG");
        assertThat(order.getOrderType()).isEqualTo(OrderType.BUY);
        assertThat(order.getQuantity()).isEqualTo(5);
        assertThat(order.getOrderedTurn()).isEqualTo(3);
        assertThat(order.getExecuteTurn()).isEqualTo(4);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @DisplayName("매도 주문을 생성하면 PENDING 상태이다")
    @Test
    void createSellOrder() {
        final StockOrder order = StockOrder.createSellOrder(1, "KAKAO", 10, 5);

        assertThat(order.getOrderType()).isEqualTo(OrderType.SELL);
        assertThat(order.getOrderedTurn()).isEqualTo(5);
        assertThat(order.getExecuteTurn()).isEqualTo(6);
        assertThat(order.isPending()).isTrue();
    }

    @DisplayName("주문을 체결하면 EXECUTED 상태가 된다")
    @Test
    void execute() {
        final StockOrder order = StockOrder.createBuyOrder(1, "SAMSUNG", 5, 3);

        order.execute();

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.EXECUTED);
        assertThat(order.isPending()).isFalse();
    }

    @DisplayName("주문을 취소하면 CANCELED 상태가 된다")
    @Test
    void cancel() {
        final StockOrder order = StockOrder.createBuyOrder(1, "SAMSUNG", 5, 3);

        order.cancel();

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThat(order.isPending()).isFalse();
    }
}
