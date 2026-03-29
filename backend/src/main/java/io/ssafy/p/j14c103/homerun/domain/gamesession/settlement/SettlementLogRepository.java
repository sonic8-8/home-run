package io.ssafy.p.j14c103.homerun.domain.gamesession.settlement;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementLogRepository extends JpaRepository<SettlementLog, Long> {

    List<SettlementLog> findAllByGameSessionIdOrderByTurnNumberAscSettlementLogIdAsc(Long gameSessionId);

    List<SettlementLog> findAllByGameSessionIdAndTurnNumberOrderBySettlementLogIdAsc(
        Long gameSessionId,
        Integer turnNumber
    );
}
