package io.ssafy.p.j14c103.homerun.api.service.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.world.response.PendingEventsProviderResponse;
import io.ssafy.p.j14c103.homerun.api.service.world.result.EventResolveExecutionResult;
import io.ssafy.p.j14c103.homerun.api.service.world.result.GameWorldResult;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import io.ssafy.p.j14c103.homerun.domain.character.GameSessionRef;
import io.ssafy.p.j14c103.homerun.domain.character.GameSessionRefRepository;
import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.domain.character.GameStatRepository;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareer;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareerRepository;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.history.event.GameEventLog;
import io.ssafy.p.j14c103.homerun.domain.history.event.GameEventLogRepository;
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
import java.time.LocalDateTime;
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
    private GameSessionRefRepository gameSessionRefRepository;

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
        gameSessionRefRepository.deleteAllInBatch();
        eventEffectRepository.deleteAllInBatch();
        eventConditionRepository.deleteAllInBatch();
        eventChoiceRepository.deleteAllInBatch();
        gameEventRepository.deleteAllInBatch();
        newsMasterRepository.deleteAllInBatch();
    }

    @DisplayName("이벤트 후보 계산부터 pending 조회, resolve 후 재조회까지 end-to-end로 검증한다")
    @Test
    void pendingResolveFlow() {
        // given
        worldContentSeedService.seed();
        gameSessionRefRepository.saveAndFlush(createGameSessionRef(4001, 12, "BOOM"));
        gameStatRepository.saveAndFlush(GameStat.create(4001, 70, 10, 10, 50, 70, 12));
        gameCareerRepository.saveAndFlush(createGameCareer(4001, 12));

        final List<GameWorldResult.EventCandidate> eventCandidates = worldEventTriggerService.calculateEventCandidates(
            4001,
            Map.of(
                "EVT-VOICE-001", new BigDecimal("0.0100"),
                "EVT-FAMILY-001", new BigDecimal("0.0200"),
                "EVT-OVERTIME-001", new BigDecimal("0.1500")
            )
        );

        worldPendingEventQueueService.enqueuePendingEvents(4001, eventCandidates);

        // when
        final PendingEventsProviderResponse pendingEvents = worldPendingEventProviderService.getPendingEvents(4001);
        final PendingEventsProviderResponse.PendingEventItem familyEvent = findEventByTitle(pendingEvents, "경조사");
        final PendingEventsProviderResponse.PendingEventChoiceItem attendChoice = findChoiceByCode(familyEvent, "A");
        final EventResolveExecutionResult resolveResult = worldEventResolveExecutionService.resolveEvent(
            4001,
            familyEvent.getEventId(),
            attendChoice.getChoiceId()
        );
        final PendingEventsProviderResponse remainingPendingEvents = worldPendingEventProviderService.getPendingEvents(4001);

        // then
        assertThat(eventCandidates).hasSize(4);
        assertThat(pendingEvents.getEvents()).hasSize(4);
        assertThat(pendingEvents.getEvents())
            .extracting(PendingEventsProviderResponse.PendingEventItem::getTitle)
            .containsExactly("보이스피싱", "야근 요청", "경조사", "열심히 일한 당신! 이직하시겠습니까?");

        final GamePendingEvent resolvedPendingEvent = gamePendingEventRepository.findById(familyEvent.getEventId())
            .orElseThrow();
        final List<GameEventLog> logs = gameEventLogRepository.findAll();

        assertThat(resolveResult.getSelectedChoiceCode()).isEqualTo("A");
        assertThat(resolveResult.getResultEffects()).hasSize(2);
        assertThat(resolveResult.getResultSummary()).isNotBlank();

        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getGameSessionId()).isEqualTo(4001);
        assertThat(logs.get(0).getSelectedChoiceCode()).isEqualTo("A");
        assertThat(logs.get(0).getResultEffects()).containsKey("effects");
        assertThat((List<?>) logs.get(0).getResultEffects().get("effects")).hasSize(2);

        assertThat(resolvedPendingEvent.isResolvedYn()).isTrue();
        assertThat(remainingPendingEvents.getEvents()).hasSize(3);
        assertThat(remainingPendingEvents.getEvents())
            .extracting(PendingEventsProviderResponse.PendingEventItem::getTitle)
            .containsExactly("보이스피싱", "야근 요청", "열심히 일한 당신! 이직하시겠습니까?");
    }

    @DisplayName("choice required 타입에 null choiceId를 보내면 로그 저장 없이 실패하고 pending 재조회 결과가 유지된다")
    @Test
    void pendingResolveFlowWithChoiceRequiredFailure() {
        // given
        worldContentSeedService.seed();
        gameSessionRefRepository.saveAndFlush(createGameSessionRef(4002, 8, "CRISIS"));
        gameStatRepository.saveAndFlush(GameStat.create(4002, 70, 10, 10, 50, 50, 8));
        gameCareerRepository.saveAndFlush(createGameCareer(4002, 1));

        final List<GameWorldResult.EventCandidate> eventCandidates = worldEventTriggerService.calculateEventCandidates(
            4002,
            Map.of(
                "EVT-VOICE-001", new BigDecimal("0.0100"),
                "EVT-FAMILY-001", new BigDecimal("0.9000"),
                "EVT-OVERTIME-001", new BigDecimal("0.0100")
            )
        );

        worldPendingEventQueueService.enqueuePendingEvents(4002, eventCandidates);
        final PendingEventsProviderResponse pendingEvents = worldPendingEventProviderService.getPendingEvents(4002);

        // when & then
        assertThat(eventCandidates).hasSize(1);
        assertThat(eventCandidates.get(0).getEventCode()).isEqualTo("EVT-VOICE-001");

        assertThat(pendingEvents.getEvents()).hasSize(1);
        final PendingEventsProviderResponse.PendingEventItem phoneEvent = pendingEvents.getEvents().get(0);
        assertThat(phoneEvent.getType()).isEqualTo(EventPresentationType.PHONE);

        assertThatThrownBy(() -> worldEventResolveExecutionService.resolveEvent(
            4002,
            phoneEvent.getEventId(),
            null
        ))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);

        final GamePendingEvent unresolvedPendingEvent = gamePendingEventRepository.findById(phoneEvent.getEventId())
            .orElseThrow();
        final PendingEventsProviderResponse pendingEventsAfterFailure = worldPendingEventProviderService.getPendingEvents(4002);

        assertThat(gameEventLogRepository.findAll()).isEmpty();
        assertThat(unresolvedPendingEvent.isResolvedYn()).isFalse();
        assertThat(pendingEventsAfterFailure.getEvents()).hasSize(1);
        assertThat(pendingEventsAfterFailure.getEvents().get(0).getEventId()).isEqualTo(phoneEvent.getEventId());
        assertThat(pendingEventsAfterFailure.getEvents().get(0).getTitle()).isEqualTo("보이스피싱");
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

    private GameSessionRef createGameSessionRef(
        final int gameSessionId,
        final int currentTurn,
        final String economicCycleType
    ) {
        return GameSessionRef.builder()
            .gameId(gameSessionId)
            .userId(1)
            .characterName("윤서")
            .characterType(CharacterType.FEMALE)
            .jobTypeSummary(JobType.STARTUP)
            .housingType(HousingType.STUDIO)
            .currentTurn(currentTurn)
            .economicCycleType(economicCycleType)
            .currentDate(LocalDate.of(2026, 1, 1))
            .cash(2_000_000)
            .netAssets(2_000_000)
            .inProgress(true)
            .bankrupt(false)
            .cleared(false)
            .createdAt(LocalDateTime.of(2026, 3, 1, 9, 0))
            .lastPlayedAt(LocalDateTime.of(2026, 3, 1, 9, 30))
            .saveSlotId(1)
            .targetRegionCode("SEOUL")
            .targetDistrictCode("GANGNAM")
            .seedType("NORMAL")
            .sessionStatus("IN_PROGRESS")
            .ownedPropertyListingId(0)
            .build();
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
