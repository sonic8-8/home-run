package io.ssafy.p.j14c103.homerun.domain.gamesession.turn;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameSessionTurnSlotRepository extends JpaRepository<GameTurnSlot, Long> {

    List<GameTurnSlot> findAllByGameSessionIdAndTurnNumberOrderBySlotIndex(Long gameSessionId, Integer turnNumber);
}
