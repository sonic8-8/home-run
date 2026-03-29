package io.ssafy.p.j14c103.homerun.api.service.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.world.response.PendingEventsProviderResponse;
import io.ssafy.p.j14c103.homerun.api.service.world.result.GameWorldResult;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.domain.character.GameStatRepository;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareer;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareerRepository;
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

class WorldPendingEventProviderServiceTest extends IntegrationTestSupport {

    @Autowired
    private WorldPendingEventProviderService worldPendingEventProviderService;

    @Autowired
    private WorldPendingEventQueueService worldPendingEventQueueService;

    @Autowired
    private WorldContentSeedService worldContentSeedService;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private GameStatRepository gameStatRepository;

    @Autowired
    private GameCareerRepository gameCareerRepository;

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
        gameCareerRepository.deleteAllInBatch();
        gameStatRepository.deleteAllInBatch();
        gameSessionRepository.deleteAllInBatch();
        eventEffectRepository.deleteAllInBatch();
        eventConditionRepository.deleteAllInBatch();
        eventChoiceRepository.deleteAllInBatch();
        gameEventRepository.deleteAllInBatch();
        newsMasterRepository.deleteAllInBatch();
    }

    @DisplayName("pending 이벤트가 없으면 빈 배열 계약을 반환한다")
    @Test
    void getPendingEventsWithoutEvents() {
        // given
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(8));

        // when
        final PendingEventsProviderResponse response = worldPendingEventProviderService.getPendingEvents(
            gameSession.getGameSessionId()
        );

        // then
        assertThat(response.getEvents()).isEmpty();
    }

    @DisplayName("pending 이벤트가 있으면 FIFO 순서와 공통 필드를 유지한 배열을 반환한다")
    @Test
    void getPendingEvents() {
        // given
        worldContentSeedService.seed();
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(12));

        final GameEvent familyEvent = findEventByCode("EVT-FAMILY-001");
        final GameEvent voiceEvent = findEventByCode("EVT-VOICE-001");
        final GameEvent jobEvent = findEventByCode("EVT-JOB-001");

        worldPendingEventQueueService.enqueuePendingEvents(
            gameSession.getGameSessionId(),
            List.of(
                toEventCandidate(familyEvent),
                toEventCandidate(voiceEvent),
                toEventCandidate(jobEvent)
            )
        );

        // when
        final PendingEventsProviderResponse response = worldPendingEventProviderService.getPendingEvents(
            gameSession.getGameSessionId()
        );

        // then
        assertThat(response.getEvents()).hasSize(3);
        assertThat(response.getEvents())
            .extracting(PendingEventsProviderResponse.PendingEventItem::getTitle)
            .containsExactly("경조사", "보이스피싱", "열심히 일한 당신! 이직하시겠습니까?");
        assertThat(response.getEvents())
            .extracting(PendingEventsProviderResponse.PendingEventItem::getEventId)
            .doesNotContainNull();
        assertThat(response.getEvents()).allSatisfy(event ->
            assertThat(event.getChoices()).hasSize(2)
        );
    }

    @DisplayName("타입별 nullable 필드를 spec 기준으로 매핑한다")
    @Test
    void getPendingEventsWithNullableFields() {
        // given
        worldContentSeedService.seed();
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(15));
        gameStatRepository.saveAndFlush(GameStat.create(gameSession.getGameSessionId().intValue(), 70, 10, 10, 50, 70, 15));
        gameCareerRepository.saveAndFlush(createGameCareer(gameSession.getGameSessionId().intValue(), 15));

        final GameEvent voiceEvent = findEventByCode("EVT-VOICE-001");
        final GameEvent burnoutEvent = findEventByCode("EVT-STATUS-001");
        final GameEvent overtimeEvent = findEventByCode("EVT-OVERTIME-001");
        final GameEvent jobEvent = findEventByCode("EVT-JOB-001");

        worldPendingEventQueueService.enqueuePendingEvents(
            gameSession.getGameSessionId(),
            List.of(
                toEventCandidate(voiceEvent),
                toEventCandidate(burnoutEvent),
                toEventCandidate(overtimeEvent),
                toEventCandidate(jobEvent)
            )
        );

        // when
        final PendingEventsProviderResponse response = worldPendingEventProviderService.getPendingEvents(
            gameSession.getGameSessionId()
        );

        // then
        final PendingEventsProviderResponse.PendingEventItem phoneEvent = findEventByType(
            response,
            EventPresentationType.PHONE
        );
        assertThat(phoneEvent.getDescription()).isEqualTo("검찰을 사칭하며 계좌 이체를 요구하는 전화가 왔습니다.");
        assertThat(phoneEvent.getSender()).isNull();
        assertThat(phoneEvent.getReceiver()).isNull();
        assertThat(phoneEvent.getDate()).isNull();
        assertThat(phoneEvent.getOfferedSalary()).isNull();
        assertThat(phoneEvent.getCurrentSalary()).isNull();
        assertThat(phoneEvent.getChoices()).isNotNull();

        final PendingEventsProviderResponse.PendingEventItem choiceEvent = findEventByType(
            response,
            EventPresentationType.CHOICE
        );
        assertThat(choiceEvent.getDescription()).isNotBlank();
        assertThat(choiceEvent.getSender()).isNull();
        assertThat(choiceEvent.getReceiver()).isNull();

        final PendingEventsProviderResponse.PendingEventItem letterEvent = findEventByType(
            response,
            EventPresentationType.LETTER
        );
        assertThat(letterEvent.getDescription()).isNotBlank();
        assertThat(letterEvent.getChoices()).isNull();
        assertThat(letterEvent.getSender()).isNull();
        assertThat(letterEvent.getReceiver()).isNull();
        assertThat(letterEvent.getDate()).isNull();
        assertThat(letterEvent.getOfferedSalary()).isNull();
        assertThat(letterEvent.getCurrentSalary()).isNull();

        final PendingEventsProviderResponse.PendingEventItem jobTransferEvent = findEventByType(
            response,
            EventPresentationType.JOB_TRANSFER
        );
        assertThat(jobTransferEvent.getSender()).isEqualTo("OO 기업 인사팀");
        assertThat(jobTransferEvent.getReceiver()).isEqualTo("김싸피 님");
        assertThat(jobTransferEvent.getDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(jobTransferEvent.getOfferedSalary()).isNotNull();
        assertThat(jobTransferEvent.getCurrentSalary()).isEqualTo(31_000_000);
        assertThat(jobTransferEvent.getChoices()).isNotNull();
    }

    @DisplayName("선택지는 choiceOrder 순서로 반환되고 resolve에 사용할 choiceId를 포함한다")
    @Test
    void getPendingEventsWithChoicesOrdered() {
        // given
        worldContentSeedService.seed();
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(7));

        final GameEvent voiceEvent = findEventByCode("EVT-VOICE-001");
        worldPendingEventQueueService.enqueuePendingEvents(
            gameSession.getGameSessionId(),
            List.of(toEventCandidate(voiceEvent))
        );

        // when
        final PendingEventsProviderResponse response = worldPendingEventProviderService.getPendingEvents(
            gameSession.getGameSessionId()
        );

        // then
        assertThat(response.getEvents()).hasSize(1);
        assertThat(response.getEvents().get(0).getChoices())
            .extracting(PendingEventsProviderResponse.PendingEventChoiceItem::getChoiceCode)
            .containsExactly("A", "B");
        assertThat(response.getEvents().get(0).getChoices())
            .extracting(PendingEventsProviderResponse.PendingEventChoiceItem::getChoiceId)
            .doesNotContainNull();
    }

    @DisplayName("존재하지 않는 세션이면 world 세션 조회 에러를 던진다")
    @Test
    void getPendingEventsWithUnknownSession() {
        // given
        final Long unknownSessionId = 9999L;

        // when
        // then
        assertThatThrownBy(() -> worldPendingEventProviderService.getPendingEvents(unknownSessionId))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.WORLD_SESSION_NOT_FOUND);
    }

    private PendingEventsProviderResponse.PendingEventItem findEventByType(
        final PendingEventsProviderResponse response,
        final EventPresentationType type
    ) {
        return response.getEvents().stream()
            .filter(event -> event.getType() == type)
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

    private GameCareer createGameCareer(final int gameId, final int tenureTurns) {
        return GameCareer.builder()
            .gameId(gameId)
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
