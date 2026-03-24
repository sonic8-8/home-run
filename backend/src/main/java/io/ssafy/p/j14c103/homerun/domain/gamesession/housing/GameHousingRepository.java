package io.ssafy.p.j14c103.homerun.domain.gamesession.housing;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameHousingRepository extends JpaRepository<GameHousing, Long> {
    Optional<GameHousing> findByGameSessionId(Long gameSessionId);
}
