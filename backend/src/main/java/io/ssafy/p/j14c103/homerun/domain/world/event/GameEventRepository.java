package io.ssafy.p.j14c103.homerun.domain.world.event;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameEventRepository extends JpaRepository<GameEvent, Integer> {

    Optional<GameEvent> findByEventCode(String eventCode);

    List<GameEvent> findAllByActiveYnTrueOrderByGameEventIdAsc();

    List<GameEvent> findAllByGameEventIdIn(List<Integer> gameEventIds);
}
