package io.ssafy.p.j14c103.homerun.api.service.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.world.result.GameWorldResult;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventChoiceRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventConditionRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventEffectRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventPresentationType;
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

class WorldPendingEventQueueServiceTest extends IntegrationTestSupport {

    @Autowired
    private WorldPendingEventQueueService worldPendingEventQueueService;

    @Autowired
    private WorldContentSeedService worldContentSeedService;

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
    private NewsMasterRepository newsMasterRepository;

    @AfterEach
    void tearDown() {
        gamePendingEventRepository.deleteAllInBatch();
        gameSessionRepository.deleteAllInBatch();
        eventEffectRepository.deleteAllInBatch();
        eventConditionRepository.deleteAllInBatch();
        eventChoiceRepository.deleteAllInBatch();
        gameEventRepository.deleteAllInBatch();
        newsMasterRepository.deleteAllInBatch();
    }

    @DisplayName("이벤트 후보 여러 개를 현재 턴 기준 FIFO pending queue로 저장한다")
    @Test
    void enqueuePendingEvents() {
        // given
        worldContentSeedService.seed();
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(12));

        final GameEvent familyEvent = findEventByCode("EVT-FAMILY-001");
        final GameEvent voiceEvent = findEventByCode("EVT-VOICE-001");
        final GameEvent jobEvent = findEventByCode("EVT-JOB-001");

        final List<GameWorldResult.EventCandidate> eventCandidates = List.of(
            toEventCandidate(familyEvent),
            toEventCandidate(voiceEvent),
            toEventCandidate(jobEvent)
        );

        // when
        worldPendingEventQueueService.enqueuePendingEvents(gameSession.getGameSessionId(), eventCandidates);

        // then
        final List<GamePendingEvent> pendingEvents = gamePendingEventRepository
            .findAllByGameSessionIdAndResolvedYnFalseOrderByCreatedAtAscGamePendingEventIdAsc(
                gameSession.getGameSessionId()
            );

        assertThat(pendingEvents).hasSize(3);
        assertThat(pendingEvents)
            .extracting(GamePendingEvent::getGameEventId)
            .containsExactly(
                familyEvent.getGameEventId(),
                voiceEvent.getGameEventId(),
                jobEvent.getGameEventId()
            );
        assertThat(pendingEvents)
            .extracting(GamePendingEvent::getTurnNumber)
            .containsOnly(12);
        assertThat(pendingEvents).allMatch(pendingEvent -> !pendingEvent.isResolvedYn());
    }

    @DisplayName("이벤트 후보가 없으면 pending queue는 비어 있어야 한다")
    @Test
    void enqueuePendingEventsWithoutCandidates() {
        // given
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(8));

        // when
        worldPendingEventQueueService.enqueuePendingEvents(gameSession.getGameSessionId(), List.of());

        // then
        final List<GamePendingEvent> pendingEvents = gamePendingEventRepository
            .findAllByGameSessionIdAndResolvedYnFalseOrderByCreatedAtAscGamePendingEventIdAsc(
                gameSession.getGameSessionId()
            );

        assertThat(pendingEvents).isEmpty();
    }

    @DisplayName("presentation type별 최소 payload를 저장한다")
    @Test
    void enqueuePendingEventsWithPayload() {
        // given
        worldContentSeedService.seed();
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(15));

        final GameEvent voiceEvent = findEventByCode("EVT-VOICE-001");
        final GameEvent overtimeEvent = findEventByCode("EVT-OVERTIME-001");
        final GameEvent jobEvent = findEventByCode("EVT-JOB-001");

        final List<GameWorldResult.EventCandidate> eventCandidates = List.of(
            toEventCandidate(voiceEvent),
            toEventCandidate(overtimeEvent),
            toEventCandidate(jobEvent)
        );

        // when
        worldPendingEventQueueService.enqueuePendingEvents(gameSession.getGameSessionId(), eventCandidates);

        // then
        final List<GamePendingEvent> pendingEvents = gamePendingEventRepository
            .findAllByGameSessionIdAndResolvedYnFalseOrderByCreatedAtAscGamePendingEventIdAsc(
                gameSession.getGameSessionId()
            );

        assertThat(pendingEvents).hasSize(3);

        final GamePendingEvent phonePendingEvent = findPendingEventByPresentationType(
            pendingEvents,
            EventPresentationType.PHONE
        );
        assertThat(phonePendingEvent.getPayload())
            .containsKey("description");

        final GamePendingEvent choicePendingEvent = findPendingEventByPresentationType(
            pendingEvents,
            EventPresentationType.CHOICE
        );
        assertThat(choicePendingEvent.getPayload())
            .containsKey("description");

        final GamePendingEvent jobPendingEvent = findPendingEventByPresentationType(
            pendingEvents,
            EventPresentationType.JOB_TRANSFER
        );
        assertThat(jobPendingEvent.getPayload())
            .containsKeys("sender", "receiver", "description");
    }

    @DisplayName("존재하지 않는 세션이면 world 세션 조회 에러를 던진다")
    @Test
    void enqueuePendingEventsWithUnknownSession() {
        // given
        final GameWorldResult.EventCandidate eventCandidate = GameWorldResult.EventCandidate.of(
            1,
            "EVT-VOICE-001",
            "보이스피싱",
            EventPresentationType.PHONE
        );

        // when & then
        assertThatThrownBy(() -> worldPendingEventQueueService.enqueuePendingEvents(9999L, List.of(eventCandidate)))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.WORLD_SESSION_NOT_FOUND);
    }

    private GamePendingEvent findPendingEventByPresentationType(
        final List<GamePendingEvent> pendingEvents,
        final EventPresentationType presentationType
    ) {
        return pendingEvents.stream()
            .filter(pendingEvent -> pendingEvent.getEventPresentationType() == presentationType)
            .findFirst()
            .orElseThrow();
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
