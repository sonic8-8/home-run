package io.ssafy.p.j14c103.homerun.api.service.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.world.result.EventResolveExecutionResult;
import io.ssafy.p.j14c103.homerun.api.service.world.result.GameWorldResult;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.history.event.GameEventLog;
import io.ssafy.p.j14c103.homerun.domain.history.event.GameEventLogRepository;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventChoice;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventChoiceRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventConditionRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventEffectRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.GameEvent;
import io.ssafy.p.j14c103.homerun.domain.world.event.GameEventRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.GamePendingEvent;
import io.ssafy.p.j14c103.homerun.domain.world.event.GamePendingEventRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.domain.world.news.NewsMasterRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import io.ssafy.p.j14c103.homerun.support.IntegrationTestSupport;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class WorldEventResolveExecutionServiceTest extends IntegrationTestSupport {

    @Autowired
    private WorldEventResolveExecutionService worldEventResolveExecutionService;

    @Autowired
    private WorldContentSeedService worldContentSeedService;

    @Autowired
    private WorldPendingEventQueueService worldPendingEventQueueService;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private GamePendingEventRepository gamePendingEventRepository;

    @Autowired
    private GameEventRepository gameEventRepository;

    @Autowired
    private EventChoiceRepository eventChoiceRepository;

    @Autowired
    private EventConditionRepository eventConditionRepository;

    @Autowired
    private EventEffectRepository eventEffectRepository;

    @Autowired
    private GameEventLogRepository gameEventLogRepository;

    @Autowired
    private NewsMasterRepository newsMasterRepository;

    @AfterEach
    void tearDown() {
        gameEventLogRepository.deleteAllInBatch();
        gamePendingEventRepository.deleteAllInBatch();
        gameSessionRepository.deleteAllInBatch();
        eventEffectRepository.deleteAllInBatch();
        eventConditionRepository.deleteAllInBatch();
        eventChoiceRepository.deleteAllInBatch();
        gameEventRepository.deleteAllInBatch();
        newsMasterRepository.deleteAllInBatch();
    }

    @DisplayName("resolve 성공 시 로그를 저장하고 pending 이벤트를 resolved 처리한 뒤 결과 DTO를 반환한다")
    @Test
    void resolveEvent() {
        // given
        worldContentSeedService.seed();
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(10));

        final GameEvent familyEvent = findEventByCode("EVT-FAMILY-001");
        worldPendingEventQueueService.enqueuePendingEvents(gameSession.getGameSessionId(), List.of(toEventCandidate(familyEvent)));

        final GamePendingEvent pendingEvent = findPendingEvent(gameSession.getGameSessionId());
        final EventChoice selectedChoice = eventChoiceRepository.findAllByGameEventIdOrderByChoiceOrderAsc(familyEvent.getGameEventId())
            .stream()
            .filter(choice -> "A".equals(choice.getChoiceCode()))
            .findFirst()
            .orElseThrow();

        // when
        final EventResolveExecutionResult result = worldEventResolveExecutionService.resolveEvent(
            gameSession.getGameSessionId(),
            pendingEvent.getGamePendingEventId(),
            selectedChoice.getEventChoiceId()
        );

        // then
        final GamePendingEvent resolvedPendingEvent = gamePendingEventRepository.findById(pendingEvent.getGamePendingEventId())
            .orElseThrow();
        final List<GameEventLog> logs = gameEventLogRepository.findAll();

        assertThat(resolvedPendingEvent.isResolvedYn()).isTrue();
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getGameSessionId()).isEqualTo(gameSession.getGameSessionId());
        assertThat(logs.get(0).getGameEventId()).isEqualTo(familyEvent.getGameEventId());
        assertThat(logs.get(0).getEventChoiceId()).isEqualTo(selectedChoice.getEventChoiceId());
        assertThat(logs.get(0).getSelectedChoiceCode()).isEqualTo("A");
        assertThat(logs.get(0).getResultSummary()).isNotBlank();
        assertThat(logs.get(0).getResultEffects()).containsKey("effects");
        assertThat((List<?>) logs.get(0).getResultEffects().get("effects")).hasSize(2);

        assertThat(result.getPendingEventId()).isEqualTo(pendingEvent.getGamePendingEventId());
        assertThat(result.getGameEventId()).isEqualTo(familyEvent.getGameEventId());
        assertThat(result.getEventChoiceId()).isEqualTo(selectedChoice.getEventChoiceId());
        assertThat(result.getSelectedChoiceCode()).isEqualTo("A");
        assertThat(result.getResultEffects()).hasSize(2);
        assertThat(result.getResultSummary()).isEqualTo(logs.get(0).getResultSummary());
    }

    @DisplayName("T17 검증 실패면 로그 저장과 상태 변경 없이 그대로 실패한다")
    @Test
    void resolveEventWithInvalidInput() {
        // given
        worldContentSeedService.seed();
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(8));

        final GameEvent voiceEvent = findEventByCode("EVT-VOICE-001");
        worldPendingEventQueueService.enqueuePendingEvents(gameSession.getGameSessionId(), List.of(toEventCandidate(voiceEvent)));
        final GamePendingEvent pendingEvent = findPendingEvent(gameSession.getGameSessionId());

        // when & then
        assertThatThrownBy(() -> worldEventResolveExecutionService.resolveEvent(
            gameSession.getGameSessionId(),
            pendingEvent.getGamePendingEventId(),
            null
        ))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);

        final GamePendingEvent unresolvedPendingEvent = gamePendingEventRepository.findById(pendingEvent.getGamePendingEventId())
            .orElseThrow();
        assertThat(unresolvedPendingEvent.isResolvedYn()).isFalse();
        assertThat(gameEventLogRepository.findAll()).isEmpty();
    }

    @DisplayName("이미 resolve된 이벤트를 다시 처리하면 추가 로그 없이 실패한다")
    @Test
    void resolveAlreadyResolvedEvent() {
        // given
        worldContentSeedService.seed();
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(6));

        final GameEvent familyEvent = findEventByCode("EVT-FAMILY-001");
        worldPendingEventQueueService.enqueuePendingEvents(gameSession.getGameSessionId(), List.of(toEventCandidate(familyEvent)));

        final GamePendingEvent pendingEvent = findPendingEvent(gameSession.getGameSessionId());
        final EventChoice selectedChoice = eventChoiceRepository.findAllByGameEventIdOrderByChoiceOrderAsc(familyEvent.getGameEventId())
            .get(0);

        worldEventResolveExecutionService.resolveEvent(
            gameSession.getGameSessionId(),
            pendingEvent.getGamePendingEventId(),
            selectedChoice.getEventChoiceId()
        );

        // when & then
        assertThatThrownBy(() -> worldEventResolveExecutionService.resolveEvent(
            gameSession.getGameSessionId(),
            pendingEvent.getGamePendingEventId(),
            selectedChoice.getEventChoiceId()
        ))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);

        assertThat(gameEventLogRepository.findAll()).hasSize(1);
    }

    private GamePendingEvent findPendingEvent(final Long gameSessionId) {
        return gamePendingEventRepository
            .findAllByGameSessionIdAndResolvedYnFalseOrderByCreatedAtAscGamePendingEventIdAsc(gameSessionId)
            .get(0);
    }

    private GameWorldResult.EventCandidate toEventCandidate(final GameEvent gameEvent) {
        return GameWorldResult.EventCandidate.of(
            gameEvent.getGameEventId(),
            gameEvent.getEventCode(),
            gameEvent.getEventName(),
            gameEvent.getEventPresentationType()
        );
    }

    private GameEvent findEventByCode(final String eventCode) {
        return gameEventRepository.findByEventCode(eventCode)
            .orElseThrow();
    }

    private GameSession createGameSession(final int currentTurn) {
        final GameSession gameSession = GameSession.create(
            1L,
            1,
            "윤서",
            CharacterType.FEMALE,
            JobType.STARTUP,
            HousingType.STUDIO,
            "SEOUL",
            "GANGNAM",
            101L,
            DataSourceType.PROFILE
        );
        gameSession.initializeCapital(
            Money.of(2_000_000L),
            Money.of(2_000_000L),
            LocalDate.of(2026, 1, 1),
            CyclePhase.BOOM
        );
        gameSession.advanceTurn(
            currentTurn,
            LocalDate.of(2026, 1, 1),
            Money.of(2_000_000L),
            Money.of(2_000_000L),
            CyclePhase.BOOM
        );
        return gameSession;
    }
}
