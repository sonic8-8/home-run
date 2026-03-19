package io.ssafy.p.j14c103.homerun.domain.world.event;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GameEventRepository extends JpaRepository<GameEvent, Integer> {
}
