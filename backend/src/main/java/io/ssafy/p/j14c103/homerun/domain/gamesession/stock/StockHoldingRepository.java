package io.ssafy.p.j14c103.homerun.domain.gamesession.stock;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockHoldingRepository
        extends JpaRepository<StockHolding, StockHoldingId> {

    List<StockHolding> findAllByGameSessionId(Long gameSessionId);
}
