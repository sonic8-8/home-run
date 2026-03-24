package io.ssafy.p.j14c103.homerun.api.service.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import io.ssafy.p.j14c103.homerun.api.service.world.response.PendingEventsProviderResponse;
import io.ssafy.p.j14c103.homerun.api.service.world.result.EventResolveExecutionResult;
import io.ssafy.p.j14c103.homerun.api.service.world.result.GameWorldResult;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.domain.character.GameStatRepository;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareer;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareerRepository;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.history.event.GameEventLog;
import io.ssafy.p.j14c103.homerun.domain.history.event.GameEventLogRepository;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventChoiceRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventConditionRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventEffectRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventPresentationType;
import io.ssafy.p.j14c103.homerun.domain.world.event.GameEventRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.GamePendingEvent;
import io.ssafy.p.j14c103.homerun.domain.world.event.GamePendingEventRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.domain.world.news.NewsMasterRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class WorldPendingResolveFlowIntegrationTest {

    @Autowired
    private WorldContentSeedService worldContentSeedService;

    @Autowired
    private WorldEventTriggerService worldEventTriggerService;

    @Autowired
    private WorldPendingEventQueueService worldPendingEventQueueService;

    @Autowired
    private WorldPendingEventProviderService worldPendingEventProviderService;

    @Autowired
    private WorldEventResolveExecutionService worldEventResolveExecutionService;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private GameStatRepository gameStatRepository;

    @Autowired
    private GameCareerRepository gameCareerRepository;

    @Autowired
    private GamePendingEventRepository gamePendingEventRepository;

    @Autowired
    private GameEventLogRepository gameEventLogRepository;

    @Autowired
    private EventEffectRepository eventEffectRepository;

    @Autowired
    private EventConditionRepository eventConditionRepository;

    @Autowired
    private EventChoiceRepository eventChoiceRepository;

    @Autowired
    private GameEventRepository gameEventRepository;

    @Autowired
    private NewsMasterRepository newsMasterRepository;

    @AfterEach
    void tearDown() {
        gameEventLogRepository.deleteAllInBatch();
        gamePendingEventRepository.deleteAllInBatch();
        gameCareerRepository.deleteAllInBatch();
        gameStatRepository.deleteAllInBatch();
        gameSessionRepository.deleteAllInBatch();
        eventEffectRepository.deleteAllInBatch();
        eventConditionRepository.deleteAllInBatch();
        eventChoiceRepository.deleteAllInBatch();
        gameEventRepository.deleteAllInBatch();
        newsMasterRepository.deleteAllInBatch();
    }

    @DisplayName("turn commit 이후 pending 조회와 resolve까지 최소 이벤트 흐름을 end-to-end로 검증한다")
    @Test
    void pendingResolveFlow() {
        // given
        worldContentSeedService.seed();
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(12, CyclePhase.BOOM));
        final Long sessionId = gameSession.getGameSessionId();
        final Integer gameId = sessionId.intValue();
        gameStatRepository.saveAndFlush(GameStat.create(gameId, 70, 10, 10, 50, 70, 12));
        gameCareerRepository.saveAndFlush(createGameCareer(gameId, 12));

        final List<GameWorldResult.EventCandidate> eventCandidates = worldEventTriggerService.calculateEventCandidates(
            sessionId,
            Map.of(
                "EVT-VOICE-001", new BigDecimal("0.0100"),
                "EVT-FAMILY-001", new BigDecimal("0.0200"),
                "EVT-OVERTIME-001", new BigDecimal("0.1500")
            )
        );

        worldPendingEventQueueService.enqueuePendingEvents(sessionId, eventCandidates);

        // when
        final PendingEventsProviderResponse pendingEvents = worldPendingEventProviderService.getPendingEvents(sessionId);
        final PendingEventsProviderResponse.PendingEventItem familyEvent = findEventByTitle(pendingEvents, "경조사");
        final PendingEventsProviderResponse.PendingEventChoiceItem attendChoice = findChoiceByCode(familyEvent, "A");
        final EventResolveExecutionResult resolveResult = worldEventResolveExecutionService.resolveEvent(
            sessionId,
            familyEvent.getEventId(),
            attendChoice.getChoiceId()
        );
        final PendingEventsProviderResponse remainingPendingEvents =
            worldPendingEventProviderService.getPendingEvents(sessionId);

        // then
        assertThat(eventCandidates).hasSize(4);
        assertThat(eventCandidates)
            .extracting(
                GameWorldResult.EventCandidate::getEventCode,
                GameWorldResult.EventCandidate::getEventName,
                GameWorldResult.EventCandidate::getEventPresentationType
            )
            .containsExactly(
                tuple("EVT-VOICE-001", "보이스피싱", EventPresentationType.PHONE),
                tuple("EVT-OVERTIME-001", "야근 요청", EventPresentationType.CHOICE),
                tuple("EVT-FAMILY-001", "경조사", EventPresentationType.CHOICE),
                tuple("EVT-JOB-001", "열심히 일한 당신! 이직하시겠습니까?", EventPresentationType.JOB_TRANSFER)
            );
        assertThat(pendingEvents.getEvents()).hasSize(4);
        assertThat(pendingEvents.getEvents())
            .extracting(PendingEventsProviderResponse.PendingEventItem::getTitle)
            .containsExactly("보이스피싱", "야근 요청", "경조사", "열심히 일한 당신! 이직하시겠습니까?");
        assertThat(familyEvent.getEventId()).isNotNull();
        assertThat(attendChoice.getChoiceId()).isNotNull();

        final GamePendingEvent resolvedPendingEvent = gamePendingEventRepository.findById(familyEvent.getEventId())
            .orElseThrow();
        final List<GameEventLog> logs = gameEventLogRepository.findAll();

        assertThat(resolveResult.getPendingEventId()).isEqualTo(familyEvent.getEventId());
        assertThat(resolveResult.getGameEventId()).isEqualTo(resolvedPendingEvent.getGameEventId());
        assertThat(resolveResult.getEventChoiceId()).isEqualTo(attendChoice.getChoiceId());
        assertThat(resolveResult.getSelectedChoiceCode()).isEqualTo("A");
        assertThat(resolveResult.getResultEffects()).hasSize(2);
        assertThat(resolveResult.getResultSummary()).isNotBlank();

        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getGameSessionId()).isEqualTo(sessionId);
        assertThat(logs.get(0).getTurnNumber()).isEqualTo(12);
        assertThat(logs.get(0).getGameEventId()).isEqualTo(resolveResult.getGameEventId());
        assertThat(logs.get(0).getEventChoiceId()).isEqualTo(resolveResult.getEventChoiceId());
        assertThat(logs.get(0).getSelectedChoiceCode()).isEqualTo("A");
        assertThat(logs.get(0).getResultEffects()).containsKey("effects");
        assertThat((List<?>) logs.get(0).getResultEffects().get("effects")).hasSize(2);
        assertThat(logs.get(0).getResultSummary()).isEqualTo(resolveResult.getResultSummary());

        assertThat(resolvedPendingEvent.isResolvedYn()).isTrue();
        assertThat(remainingPendingEvents.getEvents()).hasSize(3);
        assertThat(remainingPendingEvents.getEvents())
            .extracting(PendingEventsProviderResponse.PendingEventItem::getTitle)
            .containsExactly("보이스피싱", "야근 요청", "열심히 일한 당신! 이직하시겠습니까?");
    }

    @DisplayName("choice required 타입에 null choiceId를 보내면 로그 저장 없이 실패하고 pending 재조회 결과가 그대로 유지된다")
    @Test
    void pendingResolveFlowWithChoiceRequiredFailure() {
        // given
        worldContentSeedService.seed();
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(8, CyclePhase.CRISIS));
        final Long sessionId = gameSession.getGameSessionId();
        final Integer gameId = sessionId.intValue();
        gameStatRepository.saveAndFlush(GameStat.create(gameId, 70, 10, 10, 50, 50, 8));
        gameCareerRepository.saveAndFlush(createGameCareer(gameId, 1));

        final List<GameWorldResult.EventCandidate> eventCandidates = worldEventTriggerService.calculateEventCandidates(
            sessionId,
            Map.of(
                "EVT-VOICE-001", new BigDecimal("0.0100"),
                "EVT-FAMILY-001", new BigDecimal("0.9000"),
                "EVT-OVERTIME-001", new BigDecimal("0.0100")
            )
        );

        worldPendingEventQueueService.enqueuePendingEvents(sessionId, eventCandidates);
        final PendingEventsProviderResponse pendingEvents = worldPendingEventProviderService.getPendingEvents(sessionId);

        // when & then
        assertThat(eventCandidates).hasSize(1);
        assertThat(eventCandidates.get(0).getEventCode()).isEqualTo("EVT-VOICE-001");

        assertThat(pendingEvents.getEvents()).hasSize(1);
        final PendingEventsProviderResponse.PendingEventItem phoneEvent = pendingEvents.getEvents().get(0);
        assertThat(phoneEvent.getType()).isEqualTo(EventPresentationType.PHONE);
        assertThat(phoneEvent.getChoices())
            .extracting(
                PendingEventsProviderResponse.PendingEventChoiceItem::getChoiceCode,
                PendingEventsProviderResponse.PendingEventChoiceItem::getChoiceName
            )
            .containsExactly(
                tuple("A", "무시한다"),
                tuple("B", "지시에 따른다")
            );

        assertThatThrownBy(() -> worldEventResolveExecutionService.resolveEvent(
            sessionId,
            phoneEvent.getEventId(),
            null
        ))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);

        final GamePendingEvent unresolvedPendingEvent = gamePendingEventRepository.findById(phoneEvent.getEventId())
            .orElseThrow();
        final PendingEventsProviderResponse pendingEventsAfterFailure =
            worldPendingEventProviderService.getPendingEvents(sessionId);

        assertThat(gameEventLogRepository.findAll()).isEmpty();
        assertThat(unresolvedPendingEvent.isResolvedYn()).isFalse();
        assertThat(pendingEventsAfterFailure.getEvents()).hasSize(1);
        assertThat(pendingEventsAfterFailure.getEvents())
            .extracting(
                PendingEventsProviderResponse.PendingEventItem::getEventId,
                PendingEventsProviderResponse.PendingEventItem::getTitle,
                PendingEventsProviderResponse.PendingEventItem::getType
            )
            .containsExactly(
                tuple(phoneEvent.getEventId(), "보이스피싱", EventPresentationType.PHONE)
            );
    }

    private PendingEventsProviderResponse.PendingEventItem findEventByTitle(
        final PendingEventsProviderResponse pendingEvents,
        final String title
    ) {
        return pendingEvents.getEvents().stream()
            .filter(event -> title.equals(event.getTitle()))
            .findFirst()
            .orElseThrow();
    }

    private PendingEventsProviderResponse.PendingEventChoiceItem findChoiceByCode(
        final PendingEventsProviderResponse.PendingEventItem event,
        final String choiceCode
    ) {
        return event.getChoices().stream()
            .filter(choice -> choiceCode.equals(choice.getChoiceCode()))
            .findFirst()
            .orElseThrow();
    }

    private GameSession createGameSession(final int currentTurn, final CyclePhase cyclePhase) {
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
            cyclePhase
        );
        gameSession.advanceTurn(
            currentTurn,
            LocalDate.of(2026, 1, 1),
            Money.of(2_000_000L),
            Money.of(2_000_000L),
            cyclePhase
        );
        return gameSession;
    }

    private GameCareer createGameCareer(final int gameSessionId, final int tenureTurns) {
        return GameCareer.builder()
            .gameId(gameSessionId)
            .jobType(JobType.STARTUP)
            .jobTitle("사원")
            .salary(31_000_000)
            .tenureTurns(tenureTurns)
            .recentStudyCount(0)
            .recentNetworkingCount(0)
            .negotiationPreparationScore(0)
            .lastNegotiatedTurn(0)
            .employmentStatus(EmploymentStatus.EMPLOYED)
            .probationEndTurn(null)
            .rehireAvailableTurn(null)
            .remainingUnemploymentBenefitTurns(0)
            .salaryBeforeResignation(null)
            .build();
    }
}
