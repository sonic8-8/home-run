package io.ssafy.p.j14c103.homerun.api.service.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.world.result.EventResolveResult;
import io.ssafy.p.j14c103.homerun.api.service.world.result.GameWorldResult;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventChoice;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventChoiceRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventConditionRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventEffect;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventEffectRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventPresentationType;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventTriggerType;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class WorldEventResolveServiceTest extends IntegrationTestSupport {

    @Autowired
    private WorldEventResolveService worldEventResolveService;

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

    @DisplayName("CHOICE, PHONE, JOB_TRANSFER는 choiceId가 없으면 실패한다")
    @Test
    void resolveEventWithoutChoiceIdForRequiredChoiceType() {
        // given
        worldContentSeedService.seed();
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(12));

        final GameEvent voiceEvent = findEventByCode("EVT-VOICE-001");
        worldPendingEventQueueService.enqueuePendingEvents(gameSession.getGameSessionId(), List.of(toEventCandidate(voiceEvent)));
        final GamePendingEvent pendingEvent = findPendingEvent(gameSession.getGameSessionId());

        // when
        // then
        assertThatThrownBy(() -> worldEventResolveService.resolveEvent(gameSession.getGameSessionId(), pendingEvent.getGamePendingEventId(), null))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @DisplayName("LETTER는 choiceId 없이 resolve 가능하고 공통 효과 계산 결과를 반환한다")
    @Test
    void resolveLetterEventWithoutChoiceId() {
        // given
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(9));
        final GameEvent letterEvent = gameEventRepository.saveAndFlush(
            GameEvent.create(
                "LETTER_EVENT",
                "EVT-LETTER-001",
                "합격 편지",
                EventPresentationType.LETTER,
                EventTriggerType.PROBABILITY,
                null,
                false,
                "/images/events/letter.png",
                "SSAFY",
                "윤서",
                "합격을 축하하는 편지가 도착했습니다.",
                true
            )
        );
        eventEffectRepository.saveAndFlush(
            EventEffect.create(
                letterEvent.getGameEventId(),
                null,
                1,
                "IMMEDIATE",
                "game_stats",
                "happiness",
                "ADD",
                3,
                null,
                null,
                null,
                null,
                "편지 확인 보상"
            )
        );
        final GamePendingEvent pendingEvent = gamePendingEventRepository.saveAndFlush(
            GamePendingEvent.create(
                gameSession.getGameSessionId(),
                9,
                letterEvent.getGameEventId(),
                EventPresentationType.LETTER,
                Map.of("description", "합격 편지가 도착했습니다."),
                false,
                LocalDateTime.of(2026, 3, 20, 10, 0)
            )
        );

        // when
        final EventResolveResult result = worldEventResolveService.resolveEvent(
            gameSession.getGameSessionId(),
            pendingEvent.getGamePendingEventId(),
            null
        );

        // then
        assertThat(result.getPendingEventId()).isEqualTo(pendingEvent.getGamePendingEventId());
        assertThat(result.getGameEventId()).isEqualTo(letterEvent.getGameEventId());
        assertThat(result.getEventChoiceId()).isNull();
        assertThat(result.getSelectedChoiceCode()).isNull();
        assertThat(result.getResultEffects()).hasSize(1);
        assertThat(result.getResultEffects().get(0).getTargetColumnName()).isEqualTo("happiness");
        assertThat(result.getResultEffects().get(0).getBaseNumberValue()).isEqualTo(3);
    }

    @DisplayName("다른 이벤트 소속 choiceId를 보내면 실패한다")
    @Test
    void resolveEventWithChoiceIdFromAnotherEvent() {
        // given
        worldContentSeedService.seed();
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(14));

        final GameEvent familyEvent = findEventByCode("EVT-FAMILY-001");
        final GameEvent voiceEvent = findEventByCode("EVT-VOICE-001");
        worldPendingEventQueueService.enqueuePendingEvents(gameSession.getGameSessionId(), List.of(toEventCandidate(familyEvent)));

        final GamePendingEvent pendingEvent = findPendingEvent(gameSession.getGameSessionId());
        final Integer invalidChoiceId = eventChoiceRepository.findAllByGameEventIdOrderByChoiceOrderAsc(voiceEvent.getGameEventId())
            .get(0)
            .getEventChoiceId();

        // when & then
        assertThatThrownBy(() -> worldEventResolveService.resolveEvent(
            gameSession.getGameSessionId(),
            pendingEvent.getGamePendingEventId(),
            invalidChoiceId
        ))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @DisplayName("이미 resolved 된 pending 이벤트는 중복 resolve 할 수 없다")
    @Test
    void resolveAlreadyResolvedEvent() {
        // given
        worldContentSeedService.seed();
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(11));

        final GameEvent voiceEvent = findEventByCode("EVT-VOICE-001");
        final EventChoice validChoice = eventChoiceRepository.findAllByGameEventIdOrderByChoiceOrderAsc(voiceEvent.getGameEventId())
            .get(0);
        final GamePendingEvent resolvedPendingEvent = gamePendingEventRepository.saveAndFlush(
            GamePendingEvent.create(
                gameSession.getGameSessionId(),
                11,
                voiceEvent.getGameEventId(),
                EventPresentationType.PHONE,
                Map.of("description", voiceEvent.getDescription()),
                true,
                LocalDateTime.of(2026, 3, 20, 11, 0)
            )
        );

        // when & then
        assertThatThrownBy(() -> worldEventResolveService.resolveEvent(
            gameSession.getGameSessionId(),
            resolvedPendingEvent.getGamePendingEventId(),
            validChoice.getEventChoiceId()
        ))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @DisplayName("정상 resolve면 선택지와 effect order 기준 효과 계산 결과를 반환한다")
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
        final EventResolveResult result = worldEventResolveService.resolveEvent(
            gameSession.getGameSessionId(),
            pendingEvent.getGamePendingEventId(),
            selectedChoice.getEventChoiceId()
        );

        // then
        assertThat(result.getPendingEventId()).isEqualTo(pendingEvent.getGamePendingEventId());
        assertThat(result.getGameSessionId()).isEqualTo(gameSession.getGameSessionId());
        assertThat(result.getTurnNumber()).isEqualTo(10);
        assertThat(result.getGameEventId()).isEqualTo(familyEvent.getGameEventId());
        assertThat(result.getEventChoiceId()).isEqualTo(selectedChoice.getEventChoiceId());
        assertThat(result.getSelectedChoiceCode()).isEqualTo("A");
        assertThat(result.getResultEffects())
            .extracting(EventResolveResult.ResolvedEffect::getEffectOrder)
            .containsExactly(1, 2);
        assertThat(result.getResultEffects())
            .extracting(EventResolveResult.ResolvedEffect::getTargetColumnName)
            .containsExactly("cash", "happiness");
        assertThat(result.getResultEffects())
            .extracting(EventResolveResult.ResolvedEffect::getBaseNumberValue)
            .containsExactly(-150_000, 5);
    }

    @DisplayName("입원 이벤트를 일반 진료로 resolve하면 cash와 health 효과를 계산한다")
    @Test
    void resolveHospitalizationEvent() {
        // given
        worldContentSeedService.seed();
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(13));

        final GameEvent hospitalizationEvent = findEventByCode("EVT-STATUS-002");
        final EventChoice selectedChoice = eventChoiceRepository
            .findAllByGameEventIdOrderByChoiceOrderAsc(hospitalizationEvent.getGameEventId())
            .stream()
            .filter(choice -> "A".equals(choice.getChoiceCode()))
            .findFirst()
            .orElseThrow();
        final GamePendingEvent pendingEvent = gamePendingEventRepository.saveAndFlush(
            GamePendingEvent.create(
                gameSession.getGameSessionId(),
                13,
                hospitalizationEvent.getGameEventId(),
                EventPresentationType.CHOICE,
                Map.of("description", hospitalizationEvent.getDescription()),
                false,
                LocalDateTime.of(2026, 3, 20, 13, 0)
            )
        );

        // when
        final EventResolveResult result = worldEventResolveService.resolveEvent(
            gameSession.getGameSessionId(),
            pendingEvent.getGamePendingEventId(),
            selectedChoice.getEventChoiceId()
        );

        // then
        assertThat(result.getSelectedChoiceCode()).isEqualTo("A");
        assertThat(result.getResultEffects())
            .extracting(
                EventResolveResult.ResolvedEffect::getTargetColumnName,
                EventResolveResult.ResolvedEffect::getBaseNumberValue
            )
            .containsExactly(
                org.assertj.core.groups.Tuple.tuple("cash", -500_000),
                org.assertj.core.groups.Tuple.tuple("health", 10)
            );
    }

    @DisplayName("번아웃 이벤트는 choiceId 없이 resolve 가능하고 note effect를 반환한다")
    @Test
    void resolveBurnoutEventWithoutChoice() {
        // given
        worldContentSeedService.seed();
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(15));

        final GameEvent burnoutEvent = findEventByCode("EVT-STATUS-001");
        final GamePendingEvent pendingEvent = gamePendingEventRepository.saveAndFlush(
            GamePendingEvent.create(
                gameSession.getGameSessionId(),
                15,
                burnoutEvent.getGameEventId(),
                EventPresentationType.LETTER,
                Map.of("description", burnoutEvent.getDescription()),
                false,
                LocalDateTime.of(2026, 3, 20, 15, 0)
            )
        );

        // when
        final EventResolveResult result = worldEventResolveService.resolveEvent(
            gameSession.getGameSessionId(),
            pendingEvent.getGamePendingEventId(),
            null
        );

        // then
        assertThat(result.getEventChoiceId()).isNull();
        assertThat(result.getResultEffects()).hasSize(1);
        assertThat(result.getResultEffects().get(0).getNote())
            .contains("캐릭터 도메인");
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
