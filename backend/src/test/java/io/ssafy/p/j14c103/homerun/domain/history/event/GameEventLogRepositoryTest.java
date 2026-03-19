package io.ssafy.p.j14c103.homerun.domain.history.event;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.domain.world.event.EventChoice;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventChoiceRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventPresentationType;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventTriggerType;
import io.ssafy.p.j14c103.homerun.domain.world.event.GameEvent;
import io.ssafy.p.j14c103.homerun.domain.world.event.GameEventRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class GameEventLogRepositoryTest {

    @Autowired
    private GameEventRepository gameEventRepository;

    @Autowired
    private EventChoiceRepository eventChoiceRepository;

    @Autowired
    private GameEventLogRepository gameEventLogRepository;

    @Autowired
    private TestEntityManager entityManager;

    @DisplayName("GameEventLog를 저장하면 선택 결과와 resultEffects를 다시 조회할 수 있다")
    @Test
    void saveGameEventLog() {
        // given
        GameEvent gameEvent = gameEventRepository.saveAndFlush(
            GameEvent.create(
                "VOICE_PHISHING",
                "EVT-VOICE-001",
                "보이스피싱 전화",
                EventPresentationType.PHONE,
                EventTriggerType.PROBABILITY,
                null,
                true,
                null,
                "금감원 사칭범",
                "김싸피",
                "의심스러운 전화가 걸려왔다.",
                true
            )
        );
        EventChoice eventChoice = eventChoiceRepository.saveAndFlush(
            EventChoice.create(gameEvent.getGameEventId(), "HANG_UP", "끊는다", 1, "전화를 바로 끊는다.")
        );

        GameEventLog eventLog = GameEventLog.create(
            1001,
            12,
            gameEvent.getGameEventId(),
            eventChoice.getEventChoiceId(),
            "HANG_UP",
            Map.of("cash", 0, "stress", -2),
            "전화를 끊고 피해를 막았다.",
            LocalDateTime.of(2026, 3, 18, 10, 30)
        );

        // when
        GameEventLog saved = gameEventLogRepository.saveAndFlush(eventLog);
        entityManager.clear();

        Optional<GameEventLog> result = gameEventLogRepository.findById(saved.getGameEventLogId());

        // then
        assertThat(result).isPresent();
        assertThat(result.orElseThrow().getGameSessionId()).isEqualTo(1001);
        assertThat(result.orElseThrow().getTurnNumber()).isEqualTo(12);
        assertThat(result.orElseThrow().getSelectedChoiceCode()).isEqualTo("HANG_UP");
        assertThat(result.orElseThrow().getResultEffects()).containsEntry("stress", -2);
        assertThat(result.orElseThrow().getResultSummary()).isEqualTo("전화를 끊고 피해를 막았다.");
    }
}
