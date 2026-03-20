package io.ssafy.p.j14c103.homerun.api.service.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.world.result.GameWorldResult;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.GameSessionRef;
import io.ssafy.p.j14c103.homerun.domain.character.GameSessionRefRepository;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class WorldPendingEventQueueServiceTest {

    @Autowired
    private WorldPendingEventQueueService worldPendingEventQueueService;

    @Autowired
    private WorldContentSeedService worldContentSeedService;

    @Autowired
    private GameSessionRefRepository gameSessionRefRepository;

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
        gameSessionRefRepository.deleteAllInBatch();
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
        gameSessionRefRepository.saveAndFlush(createGameSessionRef(1001, 12));

        final GameEvent familyEvent = findEventByCode("EVT-FAMILY-001");
        final GameEvent voiceEvent = findEventByCode("EVT-VOICE-001");
        final GameEvent jobEvent = findEventByCode("EVT-JOB-001");

        final List<GameWorldResult.EventCandidate> eventCandidates = List.of(
            toEventCandidate(familyEvent),
            toEventCandidate(voiceEvent),
            toEventCandidate(jobEvent)
        );

        // when
        worldPendingEventQueueService.enqueuePendingEvents(1001, eventCandidates);

        // then
        final List<GamePendingEvent> pendingEvents = gamePendingEventRepository
            .findAllByGameSessionIdAndResolvedYnFalseOrderByCreatedAtAscGamePendingEventIdAsc(1001);

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
        gameSessionRefRepository.saveAndFlush(createGameSessionRef(1002, 8));

        // when
        worldPendingEventQueueService.enqueuePendingEvents(1002, List.of());

        // then
        final List<GamePendingEvent> pendingEvents = gamePendingEventRepository
            .findAllByGameSessionIdAndResolvedYnFalseOrderByCreatedAtAscGamePendingEventIdAsc(1002);

        assertThat(pendingEvents).isEmpty();
    }

    @DisplayName("presentation type별 최소 payload를 저장한다")
    @Test
    void enqueuePendingEventsWithPayload() {
        // given
        worldContentSeedService.seed();
        gameSessionRefRepository.saveAndFlush(createGameSessionRef(1003, 15));

        final GameEvent voiceEvent = findEventByCode("EVT-VOICE-001");
        final GameEvent overtimeEvent = findEventByCode("EVT-OVERTIME-001");
        final GameEvent jobEvent = findEventByCode("EVT-JOB-001");

        final List<GameWorldResult.EventCandidate> eventCandidates = List.of(
            toEventCandidate(voiceEvent),
            toEventCandidate(overtimeEvent),
            toEventCandidate(jobEvent)
        );

        // when
        worldPendingEventQueueService.enqueuePendingEvents(1003, eventCandidates);

        // then
        final List<GamePendingEvent> pendingEvents = gamePendingEventRepository
            .findAllByGameSessionIdAndResolvedYnFalseOrderByCreatedAtAscGamePendingEventIdAsc(1003);

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
        assertThatThrownBy(() -> worldPendingEventQueueService.enqueuePendingEvents(9999, List.of(eventCandidate)))
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

    private GameSessionRef createGameSessionRef(final int gameSessionId, final int currentTurn) {
        return GameSessionRef.builder()
            .gameId(gameSessionId)
            .userId(1)
            .characterName("윤서")
            .characterType(CharacterType.FEMALE)
            .jobTypeSummary(JobType.STARTUP)
            .housingType(HousingType.STUDIO)
            .currentTurn(currentTurn)
            .economicCycleType("BOOM")
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
}
