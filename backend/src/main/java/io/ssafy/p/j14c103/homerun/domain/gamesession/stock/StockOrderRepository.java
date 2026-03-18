package io.ssafy.p.j14c103.homerun.domain.gamesession.stock;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockOrderRepository extends JpaRepository<StockOrder, Integer> {

    List<StockOrder> findAllByGameSessionIdAndExecuteTurnAndOrderStatus(
            Integer gameSessionId, Integer executeTurn, OrderStatus orderStatus);

    List<StockOrder> findAllByGameSessionIdAndOrderStatus(
            Integer gameSessionId, OrderStatus orderStatus);
}
