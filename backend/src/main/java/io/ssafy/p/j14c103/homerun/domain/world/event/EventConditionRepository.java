package io.ssafy.p.j14c103.homerun.domain.world.event;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventConditionRepository extends JpaRepository<EventCondition, Integer> {

    boolean existsByGameEventId(Integer gameEventId);
}
