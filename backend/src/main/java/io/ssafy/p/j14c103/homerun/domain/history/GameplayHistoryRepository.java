package io.ssafy.p.j14c103.homerun.domain.history;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameplayHistoryRepository extends JpaRepository<GameplayHistory, Integer> {

    List<GameplayHistory> findAllByGameIdOrderByOccurredTurnAscHistoryIdAsc(Integer gameId);

    List<GameplayHistory> findAllByGameIdAndTableNameOrderByOccurredTurnAscHistoryIdAsc(
        Integer gameId,
        String tableName
    );
}
