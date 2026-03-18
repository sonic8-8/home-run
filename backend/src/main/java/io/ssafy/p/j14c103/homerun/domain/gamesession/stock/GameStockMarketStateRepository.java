package io.ssafy.p.j14c103.homerun.domain.gamesession.stock;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameStockMarketStateRepository
        extends JpaRepository<GameStockMarketState, GameStockMarketStateId> {

    List<GameStockMarketState> findAllByGameSessionId(Integer gameSessionId);
}
