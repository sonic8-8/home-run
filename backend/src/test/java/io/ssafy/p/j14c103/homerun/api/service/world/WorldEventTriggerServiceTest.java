package io.ssafy.p.j14c103.homerun.api.service.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventChoice;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventChoiceRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventCondition;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventConditionRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventEffectRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventPresentationType;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventTriggerType;
import io.ssafy.p.j14c103.homerun.domain.world.event.GameEvent;
import io.ssafy.p.j14c103.homerun.domain.world.event.GameEventRepository;
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
class WorldEventTriggerServiceTest {

    @Autowired
    private WorldEventTriggerService worldEventTriggerService;

    @Autowired
    private WorldContentSeedService worldContentSeedService;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private GameStatRepository gameStatRepository;

    @Autowired
    private GameCareerRepository gameCareerRepository;

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
        gameCareerRepository.deleteAllInBatch();
        gameStatRepository.deleteAllInBatch();
        gameSessionRepository.deleteAllInBatch();
        eventEffectRepository.deleteAllInBatch();
        eventConditionRepository.deleteAllInBatch();
        eventChoiceRepository.deleteAllInBatch();
        gameEventRepository.deleteAllInBatch();
        newsMasterRepository.deleteAllInBatch();
    }

    @DisplayName("호황 사이클과 경력 조건이 모두 맞고 확률 roll이 충분히 낮으면 이벤트 후보 4개를 계산한다")
    @Test
    void calculateEventCandidatesWhenAllRulesAreSatisfied() {
        // given
        worldContentSeedService.seed();
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(CyclePhase.BOOM));
        gameStatRepository.saveAndFlush(GameStat.create(gameSession.getGameSessionId().intValue(), 70, 10, 10, 50, 60, 12));
        gameCareerRepository.saveAndFlush(createGameCareer(gameSession.getGameSessionId().intValue(), 12));

        // when
        final List<GameWorldResult.EventCandidate> candidates = worldEventTriggerService
            .calculateEventCandidates(
                gameSession.getGameSessionId(),
                Map.of(
                    "EVT-VOICE-001", new BigDecimal("0.0100"),
                    "EVT-FAMILY-001", new BigDecimal("0.0200"),
                    "EVT-OVERTIME-001", new BigDecimal("0.1500")
                )
            );

        // then
        assertThat(candidates).hasSize(4);
        assertThat(candidates)
            .extracting(GameWorldResult.EventCandidate::getGameEventId)
            .doesNotContainNull();
        assertThat(candidates)
            .extracting(
                GameWorldResult.EventCandidate::getEventCode,
                GameWorldResult.EventCandidate::getEventName,
                GameWorldResult.EventCandidate::getEventPresentationType
            )
            .containsExactlyInAnyOrder(
                org.assertj.core.groups.Tuple.tuple("EVT-VOICE-001", "보이스피싱", EventPresentationType.PHONE),
                org.assertj.core.groups.Tuple.tuple("EVT-OVERTIME-001", "야근 요청", EventPresentationType.CHOICE),
                org.assertj.core.groups.Tuple.tuple("EVT-FAMILY-001", "경조사", EventPresentationType.CHOICE),
                org.assertj.core.groups.Tuple.tuple("EVT-JOB-001", "열심히 일한 당신! 이직하시겠습니까?", EventPresentationType.JOB_TRANSFER)
            );
    }

    @DisplayName("위기 사이클과 미달 경력 조건에서는 어떤 이벤트도 후보로 계산되지 않는다")
    @Test
    void calculateEventCandidatesWhenNoRuleIsSatisfied() {
        // given
        worldContentSeedService.seed();
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(CyclePhase.CRISIS));
        gameStatRepository.saveAndFlush(GameStat.create(gameSession.getGameSessionId().intValue(), 70, 10, 10, 50, 59, 12));
        gameCareerRepository.saveAndFlush(createGameCareer(gameSession.getGameSessionId().intValue(), 11));

        // when
        final List<GameWorldResult.EventCandidate> candidates = worldEventTriggerService
            .calculateEventCandidates(
                gameSession.getGameSessionId(),
                Map.of(
                    "EVT-VOICE-001", new BigDecimal("0.9000"),
                    "EVT-FAMILY-001", new BigDecimal("0.9000"),
                    "EVT-OVERTIME-001", new BigDecimal("0.0100")
                )
            );

        // then
        assertThat(candidates).isEmpty();
    }

    @DisplayName("지원하지 않는 비교 연산자가 있으면 서버 설정 오류로 실패한다")
    @Test
    void calculateEventCandidatesWithUnsupportedOperator() {
        // given
        worldContentSeedService.seed();
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(CyclePhase.BOOM));
        gameStatRepository.saveAndFlush(GameStat.create(gameSession.getGameSessionId().intValue(), 70, 10, 10, 50, 80, 12));
        gameCareerRepository.saveAndFlush(createGameCareer(gameSession.getGameSessionId().intValue(), 20));

        final GameEvent invalidEvent = gameEventRepository.saveAndFlush(
            GameEvent.create(
                "INVALID_TRIGGER",
                "EVT-INVALID-001",
                "잘못된 이벤트",
                EventPresentationType.CHOICE,
                EventTriggerType.CONDITION,
                null,
                true,
                null,
                null,
                null,
                "지원하지 않는 연산자 테스트",
                true
            )
        );
        eventChoiceRepository.saveAndFlush(
            EventChoice.create(
                invalidEvent.getGameEventId(),
                "A",
                "확인",
                1,
                "잘못된 조건 테스트"
            )
        );
        eventConditionRepository.saveAndFlush(
            EventCondition.create(
                invalidEvent.getGameEventId(),
                1,
                1,
                "COLUMN_COMPARISON",
                "game_stats",
                "knowledge",
                "LTE",
                null,
                new BigDecimal("80"),
                null,
                "AND"
            )
        );

        // when & then
        assertThatThrownBy(() -> worldEventTriggerService.calculateEventCandidates(gameSession.getGameSessionId(), Map.of()))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    private GameSession createGameSession(final CyclePhase cyclePhase) {
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
            12,
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
