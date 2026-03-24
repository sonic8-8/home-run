package io.ssafy.p.j14c103.homerun.domain.world.event;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventEffectRepository extends JpaRepository<EventEffect, Integer> {

    boolean existsByGameEventId(Integer gameEventId);

    List<EventEffect> findAllByGameEventIdOrderByEffectOrderAscEventEffectIdAsc(Integer gameEventId);
}
