package io.ssafy.p.j14c103.homerun.domain.character.schedule;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameTurnSlotRepository extends JpaRepository<GameTurnSlot, Integer> {

    void deleteByGameIdAndTurnNumber(Integer gameId, Integer turnNumber);

    List<GameTurnSlot> findAllByGameIdAndTurnNumberOrderBySlotIndex(Integer gameId, Integer turnNumber);
}
