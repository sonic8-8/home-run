package io.ssafy.p.j14c103.homerun.domain.world.event;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventChoiceRepository extends JpaRepository<EventChoice, Integer> {

    List<EventChoice> findAllByGameEventIdOrderByChoiceOrderAsc(Integer gameEventId);

    List<EventChoice> findAllByGameEventIdInOrderByGameEventIdAscChoiceOrderAsc(List<Integer> gameEventIds);

    Optional<EventChoice> findByEventChoiceIdAndGameEventId(Integer eventChoiceId, Integer gameEventId);
}
