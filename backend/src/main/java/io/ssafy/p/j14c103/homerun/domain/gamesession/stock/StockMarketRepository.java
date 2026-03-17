package io.ssafy.p.j14c103.homerun.domain.gamesession.stock;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMarketRepository extends JpaRepository<StockMarket, String> {

    List<StockMarket> findAllByKisStockCodeIsNotNull();
}
