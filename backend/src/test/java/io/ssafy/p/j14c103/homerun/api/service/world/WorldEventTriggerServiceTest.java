package io.ssafy.p.j14c103.homerun.api.service.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
class WorldEventTriggerServiceTest {

    @Autowired
    private WorldEventTriggerService worldEventTriggerService;

    @Autowired
    private WorldContentSeedService worldContentSeedService;

    @Autowired
    private GameSessionRefRepository gameSessionRefRepository;

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
        gameSessionRefRepository.deleteAllInBatch();
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
        gameSessionRefRepository.saveAndFlush(createGameSessionRef(1001, "BOOM"));
        gameStatRepository.saveAndFlush(GameStat.create(1001, 70, 10, 10, 50, 60, 12));
        gameCareerRepository.saveAndFlush(createGameCareer(1001, 12));

        // when
        final List<GameWorldResult.EventCandidate> candidates = worldEventTriggerService
            .calculateEventCandidates(
                1001,
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
        gameSessionRefRepository.saveAndFlush(createGameSessionRef(1002, "CRISIS"));
        gameStatRepository.saveAndFlush(GameStat.create(1002, 70, 10, 10, 50, 59, 12));
        gameCareerRepository.saveAndFlush(createGameCareer(1002, 11));

        // when
        final List<GameWorldResult.EventCandidate> candidates = worldEventTriggerService
            .calculateEventCandidates(
                1002,
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
        gameSessionRefRepository.saveAndFlush(createGameSessionRef(1003, "BOOM"));
        gameStatRepository.saveAndFlush(GameStat.create(1003, 70, 10, 10, 50, 80, 12));
        gameCareerRepository.saveAndFlush(createGameCareer(1003, 20));

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
        assertThatThrownBy(() -> worldEventTriggerService.calculateEventCandidates(1003, Map.of()))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    private GameSessionRef createGameSessionRef(final int gameSessionId, final String economicCycleType) {
        return GameSessionRef.builder()
            .gameId(gameSessionId)
            .userId(1)
            .characterName("윤서")
            .characterType(CharacterType.FEMALE)
            .jobTypeSummary(JobType.STARTUP)
            .housingType(HousingType.STUDIO)
            .currentTurn(12)
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
