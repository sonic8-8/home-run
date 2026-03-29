package io.ssafy.p.j14c103.homerun.domain.world.event;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.ssafy.p.j14c103.homerun.support.IntegrationTestSupport;

@Transactional
class GamePendingEventRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private GameEventRepository gameEventRepository;

    @Autowired
    private GamePendingEventRepository gamePendingEventRepository;

    @Autowired
    private EntityManager entityManager;

    @DisplayName("미해결 pending event는 생성 시각 순으로 조회할 수 있다")
    @Test
    void findUnresolvedPendingEventsOrderByCreatedAtAsc() {
        // given
        GameEvent gameEvent = gameEventRepository.saveAndFlush(
            GameEvent.create(
                "JOB_TRANSFER",
                "EVT-JOB-001",
                "이직 제안",
                EventPresentationType.JOB_TRANSFER,
                EventTriggerType.CYCLE,
                null,
                true,
                null,
                "OO 기업 인사팀",
                "김싸피",
                "더 높은 연봉의 이직 제안이 도착했다.",
                true
            )
        );

        gamePendingEventRepository.saveAndFlush(
            GamePendingEvent.create(
                1001L,
                5,
                gameEvent.getGameEventId(),
                EventPresentationType.JOB_TRANSFER,
                Map.of("offeredSalary", 60000000, "currentSalary", 40000000),
                false,
                LocalDateTime.of(2026, 3, 18, 10, 5)
            )
        );
        gamePendingEventRepository.saveAndFlush(
            GamePendingEvent.create(
                1001L,
                5,
                gameEvent.getGameEventId(),
                EventPresentationType.JOB_TRANSFER,
                Map.of("offeredSalary", 65000000, "currentSalary", 40000000),
                false,
                LocalDateTime.of(2026, 3, 18, 10, 0)
            )
        );
        gamePendingEventRepository.saveAndFlush(
            GamePendingEvent.create(
                1001L,
                5,
                gameEvent.getGameEventId(),
                EventPresentationType.JOB_TRANSFER,
                Map.of("offeredSalary", 70000000, "currentSalary", 40000000),
                true,
                LocalDateTime.of(2026, 3, 18, 9, 55)
            )
        );
        entityManager.clear();

        // when
        List<GamePendingEvent> result = gamePendingEventRepository
            .findAllByGameSessionIdAndResolvedYnFalseOrderByCreatedAtAscGamePendingEventIdAsc(1001L);

        // then
        assertThat(result).hasSize(2);
        assertThat(result)
            .extracting(GamePendingEvent::getCreatedAt)
            .containsExactly(
                LocalDateTime.of(2026, 3, 18, 10, 0),
                LocalDateTime.of(2026, 3, 18, 10, 5)
            );
        assertThat(result).allMatch(pendingEvent -> !pendingEvent.isResolvedYn());
    }
}
