package io.ssafy.p.j14c103.homerun.domain.world.event;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GamePendingEventRepository extends JpaRepository<GamePendingEvent, Integer> {

    List<GamePendingEvent> findAllByGameSessionIdAndResolvedYnFalseOrderByCreatedAtAscGamePendingEventIdAsc(
        Integer gameSessionId
    );
}
