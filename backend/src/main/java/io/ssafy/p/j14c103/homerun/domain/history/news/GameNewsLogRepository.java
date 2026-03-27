package io.ssafy.p.j14c103.homerun.domain.history.news;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameNewsLogRepository extends JpaRepository<GameNewsLog, Integer> {

    List<GameNewsLog> findAllByGameSessionIdOrderByTurnNumberAscGameNewsLogIdAsc(Long gameSessionId);

    List<GameNewsLog> findAllByGameSessionIdAndTurnNumberOrderByGameNewsLogIdAsc(
        Long gameSessionId,
        Integer turnNumber
    );

    List<GameNewsLog> findAllByGameSessionIdOrderByTurnNumberDescGameNewsLogIdDesc(Long gameSessionId);
}
