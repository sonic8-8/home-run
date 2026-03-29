package io.ssafy.p.j14c103.homerun.domain.gamesession;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
    boolean existsByUserIdAndSlotNumber(Long userId, Integer slotNumber);
    List<GameSession> findAllByUserIdOrderBySlotNumberAsc(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select gameSession
        from GameSession gameSession
        where gameSession.gameSessionId = :sessionId
        """)
    Optional<GameSession> findByIdForUpdate(@Param("sessionId") Long sessionId);
}
