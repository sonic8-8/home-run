package io.ssafy.p.j14c103.homerun.domain.character;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameStatRepository extends JpaRepository<GameStat, Integer> {
}
