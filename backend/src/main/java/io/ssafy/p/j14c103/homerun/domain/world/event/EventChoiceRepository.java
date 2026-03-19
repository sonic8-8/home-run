package io.ssafy.p.j14c103.homerun.domain.world.event;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventChoiceRepository extends JpaRepository<EventChoice, Integer> {

    List<EventChoice> findAllByGameEventIdOrderByChoiceOrderAsc(Integer gameEventId);
}
