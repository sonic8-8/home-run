package io.ssafy.p.j14c103.homerun.domain.gamesession.report;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameTimelineRepository extends JpaRepository<GameTimeline, Long> {

    List<GameTimeline> findAllByGameSessionIdOrderByTurnNumberAsc(Long gameSessionId);
}
