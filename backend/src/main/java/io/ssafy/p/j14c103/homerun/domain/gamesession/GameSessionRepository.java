package io.ssafy.p.j14c103.homerun.domain.gamesession;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
    boolean existsByUserIdAndSlotNumber(Long userId, Integer slotNumber);
    List<GameSession> findAllByUserIdOrderBySlotNumberAsc(Long userId);
}
