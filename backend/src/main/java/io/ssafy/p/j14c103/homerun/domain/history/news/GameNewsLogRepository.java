package io.ssafy.p.j14c103.homerun.domain.history.news;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameNewsLogRepository extends JpaRepository<GameNewsLog, Integer> {

    List<GameNewsLog> findAllByGameSessionIdOrderByTurnNumberDescGameNewsLogIdDesc(Long gameSessionId);
}
