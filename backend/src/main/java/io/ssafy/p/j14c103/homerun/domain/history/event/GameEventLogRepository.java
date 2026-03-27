package io.ssafy.p.j14c103.homerun.domain.history.event;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameEventLogRepository extends JpaRepository<GameEventLog, Integer> {

    List<GameEventLog> findAllByGameSessionIdOrderByTurnNumberAscGameEventLogIdAsc(Long gameSessionId);
}
