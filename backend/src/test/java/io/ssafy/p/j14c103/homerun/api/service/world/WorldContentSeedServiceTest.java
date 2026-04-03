package io.ssafy.p.j14c103.homerun.api.service.world;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.domain.world.event.EventChoice;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventChoiceRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventCondition;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventConditionRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventEffectRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventPresentationType;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventTriggerType;
import io.ssafy.p.j14c103.homerun.domain.world.event.GameEvent;
import io.ssafy.p.j14c103.homerun.domain.world.event.GameEventRepository;
import io.ssafy.p.j14c103.homerun.domain.world.news.NewsMasterRepository;
import io.ssafy.p.j14c103.homerun.support.IntegrationTestSupport;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class WorldContentSeedServiceTest extends IntegrationTestSupport {

    @Autowired
    private WorldContentSeedService worldContentSeedService;

    @Autowired
    private NewsMasterRepository newsMasterRepository;

    @Autowired
    private GameEventRepository gameEventRepository;

    @Autowired
    private EventChoiceRepository eventChoiceRepository;

    @Autowired
    private EventConditionRepository eventConditionRepository;

    @Autowired
    private EventEffectRepository eventEffectRepository;

    @AfterEach
    void tearDown() {
        eventEffectRepository.deleteAllInBatch();
        eventConditionRepository.deleteAllInBatch();
        eventChoiceRepository.deleteAllInBatch();
        gameEventRepository.deleteAllInBatch();
        newsMasterRepository.deleteAllInBatch();
    }

    @DisplayName("seed를 실행하면 일반 뉴스 20건과 cycle 뉴스 9건, 이벤트 12건이 적재된다")
    @Test
    void seedWorldContents() {
        // when
        worldContentSeedService.seed();

        // then
        assertThat(newsMasterRepository.count()).isEqualTo(29);
        assertThat(gameEventRepository.count()).isEqualTo(12);
        assertThat(newsMasterRepository.existsByEconomicCycleType("BOOM_TO_BOOM")).isTrue();
        assertThat(newsMasterRepository.existsByEconomicCycleType("BOOM_TO_CRISIS")).isTrue();
        assertThat(newsMasterRepository.existsByEconomicCycleType("BOOM_TO_RECOVERY")).isTrue();
        assertThat(newsMasterRepository.existsByEconomicCycleType("CRISIS_TO_CRISIS")).isTrue();
        assertThat(newsMasterRepository.existsByEconomicCycleType("CRISIS_TO_RECOVERY")).isTrue();
        assertThat(newsMasterRepository.existsByEconomicCycleType("CRISIS_TO_BOOM")).isTrue();
        assertThat(newsMasterRepository.existsByEconomicCycleType("RECOVERY_TO_RECOVERY")).isTrue();
        assertThat(newsMasterRepository.existsByEconomicCycleType("RECOVERY_TO_CRISIS")).isTrue();
        assertThat(newsMasterRepository.existsByEconomicCycleType("RECOVERY_TO_BOOM")).isTrue();
    }

    @DisplayName("같은 seed를 다시 실행해도 뉴스와 이벤트 수가 증가하지 않는다")
    @Test
    void reseedDoesNotDuplicateData() {
        // given
        worldContentSeedService.seed();
        final long newsCountBefore = newsMasterRepository.count();
        final long eventCountBefore = gameEventRepository.count();
        final long choiceCountBefore = eventChoiceRepository.count();
        final long conditionCountBefore = eventConditionRepository.count();
        final long effectCountBefore = eventEffectRepository.count();

        // when
        worldContentSeedService.seed();

        // then
        assertThat(newsMasterRepository.count()).isEqualTo(newsCountBefore);
        assertThat(gameEventRepository.count()).isEqualTo(eventCountBefore);
        assertThat(eventChoiceRepository.count()).isEqualTo(choiceCountBefore);
        assertThat(eventConditionRepository.count()).isEqualTo(conditionCountBefore);
        assertThat(eventEffectRepository.count()).isEqualTo(effectCountBefore);
    }

    @DisplayName("보이스피싱 이벤트는 PHONE, PROBABILITY, 2개 선택지 계약으로 적재된다")
    @Test
    void seedVoicePhishingEventContract() {
        // given
        worldContentSeedService.seed();

        // when
        final GameEvent event = findEventByCode("EVT-VOICE-001");
        final List<EventChoice> choices = eventChoiceRepository
            .findAllByGameEventIdOrderByChoiceOrderAsc(event.getGameEventId());

        // then
        assertThat(event.getEventPresentationType()).isEqualTo(EventPresentationType.PHONE);
        assertThat(event.getEventTriggerType()).isEqualTo(EventTriggerType.PROBABILITY);
        assertThat(event.getEventTriggerValue()).isEqualByComparingTo("0.0500");
        assertThat(choices)
            .extracting(EventChoice::getChoiceCode)
            .containsExactly("A", "B");
    }

    @DisplayName("이직 제안 이벤트는 JOB_TRANSFER, CONDITION, 2개 조건 계약으로 적재된다")
    @Test
    void seedJobTransferEventContract() {
        // given
        worldContentSeedService.seed();

        // when
        final GameEvent event = findEventByCode("EVT-JOB-001");
        final List<EventCondition> conditions = eventConditionRepository.findAll().stream()
            .filter(condition -> condition.getGameEventId().equals(event.getGameEventId()))
            .toList();

        // then
        assertThat(event.getEventPresentationType()).isEqualTo(EventPresentationType.JOB_TRANSFER);
        assertThat(event.getEventTriggerType()).isEqualTo(EventTriggerType.CONDITION);
        assertThat(conditions).hasSize(2);
        assertThat(conditions)
            .extracting(EventCondition::getTargetTableName, EventCondition::getTargetColumnName)
            .containsExactlyInAnyOrder(
                org.assertj.core.groups.Tuple.tuple("game_stats", "knowledge"),
                org.assertj.core.groups.Tuple.tuple("game_careers", "tenure_turns")
            );
    }

    @DisplayName("입원 이벤트는 CHOICE, CONDITION, health 범위 조건 계약으로 적재된다")
    @Test
    void seedHospitalizationEventContract() {
        // given
        worldContentSeedService.seed();

        // when
        final GameEvent event = findEventByCode("EVT-STATUS-002");
        final List<EventCondition> conditions = eventConditionRepository.findAll().stream()
            .filter(condition -> condition.getGameEventId().equals(event.getGameEventId()))
            .toList();

        // then
        assertThat(event.getEventPresentationType()).isEqualTo(EventPresentationType.CHOICE);
        assertThat(event.getEventTriggerType()).isEqualTo(EventTriggerType.CONDITION);
        assertThat(conditions)
            .extracting(EventCondition::getTargetTableName, EventCondition::getTargetColumnName, EventCondition::getComparisonOperator)
            .containsExactlyInAnyOrder(
                org.assertj.core.groups.Tuple.tuple("game_stats", "health", "GTE"),
                org.assertj.core.groups.Tuple.tuple("game_stats", "health", "LTE")
            );
    }

    @DisplayName("금리 변동 이벤트는 RATE_HIKE cycle_type 조건과 2개 선택지 계약으로 적재된다")
    @Test
    void seedRateChangeEventContract() {
        // given
        worldContentSeedService.seed();

        // when
        final GameEvent event = findEventByCode("EVT-RATE-001");
        final List<EventChoice> choices = eventChoiceRepository
            .findAllByGameEventIdOrderByChoiceOrderAsc(event.getGameEventId());
        final List<EventCondition> conditions = eventConditionRepository.findAll().stream()
            .filter(condition -> condition.getGameEventId().equals(event.getGameEventId()))
            .toList();

        // then
        assertThat(event.getEventPresentationType()).isEqualTo(EventPresentationType.CHOICE);
        assertThat(event.getEventTriggerType()).isEqualTo(EventTriggerType.CYCLE);
        assertThat(choices)
            .extracting(EventChoice::getChoiceCode)
            .containsExactly("A", "B");
        assertThat(conditions)
            .extracting(EventCondition::getTargetTableName, EventCondition::getTargetColumnName, EventCondition::getCriteriaTextValue)
            .containsExactly(
                org.assertj.core.groups.Tuple.tuple("game_sessions", "cycle_type", "CYCLE_RATE_HIKE")
            );
    }

    private GameEvent findEventByCode(final String eventCode) {
        return gameEventRepository.findByEventCode(eventCode)
            .orElseThrow(() -> new AssertionError("eventCode=" + eventCode + " 데이터를 찾지 못했습니다."));
    }
}
