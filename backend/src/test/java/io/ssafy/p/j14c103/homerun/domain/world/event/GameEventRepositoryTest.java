package io.ssafy.p.j14c103.homerun.domain.world.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class GameEventRepositoryTest {

    @Autowired
    private GameEventRepository gameEventRepository;

    @Autowired
    private EventChoiceRepository eventChoiceRepository;

    @Autowired
    private TestEntityManager entityManager;

    @DisplayName("GameEvent를 저장하면 이벤트 메타데이터를 다시 조회할 수 있다")
    @Test
    void saveGameEvent() {
        // given
        GameEvent gameEvent = GameEvent.create(
            "VOICE_PHISHING",
            "EVT-VOICE-001",
            "보이스피싱 전화",
            EventPresentationType.PHONE,
            EventTriggerType.PROBABILITY,
            new BigDecimal("20.0000"),
            true,
            "/images/events/voice-phishing.png",
            "금감원 사칭범",
            "김싸피",
            "의심스러운 전화가 걸려왔다.",
            true
        );

        // when
        GameEvent saved = gameEventRepository.saveAndFlush(gameEvent);
        entityManager.clear();

        GameEvent found = gameEventRepository.findById(saved.getGameEventId())
            .orElseThrow();

        // then
        assertThat(found.getEventTypeCode()).isEqualTo("VOICE_PHISHING");
        assertThat(found.getEventCode()).isEqualTo("EVT-VOICE-001");
        assertThat(found.getEventName()).isEqualTo("보이스피싱 전화");
        assertThat(found.getEventPresentationType()).isEqualTo(EventPresentationType.PHONE);
        assertThat(found.getEventTriggerType()).isEqualTo(EventTriggerType.PROBABILITY);
        assertThat(found.getEventTriggerValue()).isEqualByComparingTo("20.0000");
        assertThat(found.isChoiceRequiredYn()).isTrue();
        assertThat(found.getSenderName()).isEqualTo("금감원 사칭범");
        assertThat(found.getReceiverName()).isEqualTo("김싸피");
        assertThat(found.isActiveYn()).isTrue();
    }

    @DisplayName("EventChoice는 같은 이벤트 안에서 choiceOrder 기준으로 정렬 조회할 수 있다")
    @Test
    void findEventChoicesByGameEventIdOrderByChoiceOrderAsc() {
        // given
        GameEvent gameEvent = gameEventRepository.saveAndFlush(
            GameEvent.create(
                "OVERTIME_REQUEST",
                "EVT-OVERTIME-001",
                "야근 요청",
                EventPresentationType.CHOICE,
                EventTriggerType.CONDITION,
                null,
                true,
                null,
                "팀장",
                "김싸피",
                "마감 일정 때문에 야근 요청이 들어왔다.",
                true
            )
        );

        eventChoiceRepository.saveAndFlush(
            EventChoice.create(gameEvent.getGameEventId(), "DECLINE", "거절한다", 2, "건강을 우선한다.")
        );
        eventChoiceRepository.saveAndFlush(
            EventChoice.create(gameEvent.getGameEventId(), "ACCEPT", "수락한다", 1, "평판을 위해 수락한다.")
        );
        entityManager.clear();

        // when
        List<EventChoice> result = eventChoiceRepository
            .findAllByGameEventIdOrderByChoiceOrderAsc(gameEvent.getGameEventId());

        // then
        assertThat(result).hasSize(2);
        assertThat(result)
            .extracting(EventChoice::getChoiceCode)
            .containsExactly("ACCEPT", "DECLINE");
        assertThat(result)
            .extracting(EventChoice::getChoiceName)
            .containsExactly("수락한다", "거절한다");
    }
}
