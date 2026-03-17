package io.ssafy.p.j14c103.homerun.domain.character;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareer;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareerRepository;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.character.schedule.ActionCategory;
import io.ssafy.p.j14c103.homerun.domain.character.schedule.ActionType;
import io.ssafy.p.j14c103.homerun.domain.character.schedule.GameTurnSlot;
import io.ssafy.p.j14c103.homerun.domain.character.schedule.GameTurnSlotRepository;
import io.ssafy.p.j14c103.homerun.domain.history.GameplayHistory;
import io.ssafy.p.j14c103.homerun.domain.history.GameplayHistoryRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class CharacterPersistenceRepositoryTest {

    @Autowired
    private GameSessionRefRepository gameSessionRefRepository;

    @Autowired
    private GameStatRepository gameStatRepository;

    @Autowired
    private GameCareerRepository gameCareerRepository;

    @Autowired
    private GameTurnSlotRepository gameTurnSlotRepository;

    @Autowired
    private GameplayHistoryRepository gameplayHistoryRepository;

    @DisplayName("게임 세션 참조를 한국어 테이블과 컬럼으로 저장하고 조회할 수 있다.")
    @Test
    void saveAndLoadGameSessionRef() {
        // given
        GameSessionRef gameSessionRef = GameSessionRef.builder()
            .gameId(1001)
            .userId(1)
            .characterName("윤서")
            .characterType(CharacterType.FEMALE)
            .jobTypeSummary(JobType.STARTUP)
            .housingType(HousingType.STUDIO)
            .currentTurn(1)
            .economicCycleType("RECOVERY")
            .currentDate(LocalDate.of(2026, 3, 1))
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

        gameSessionRefRepository.save(gameSessionRef);

        // when
        GameSessionRef result = gameSessionRefRepository.findById(1001)
            .orElseThrow();

        // then
        assertThat(result.getCharacterType()).isEqualTo(CharacterType.FEMALE);
        assertThat(result.getJobTypeSummary()).isEqualTo(JobType.STARTUP);
        assertThat(result.getHousingType()).isEqualTo(HousingType.STUDIO);
        assertThat(result.getCurrentDate()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(result.isInProgress()).isTrue();
    }

    @DisplayName("게임 스탯을 저장하고 조회할 수 있다.")
    @Test
    void saveAndLoadGameStat() {
        // given
        GameStat gameStat = GameStat.builder()
            .gameId(1001)
            .health(70)
            .fatigue(30)
            .stress(20)
            .happiness(55)
            .knowledge(40)
            .burnout(false)
            .burnoutStartedTurn(null)
            .hospitalizedUntilTurn(null)
            .build();

        gameStatRepository.save(gameStat);

        // when
        GameStat result = gameStatRepository.findById(1001)
            .orElseThrow();

        // then
        assertThat(result.getHealth()).isEqualTo(70);
        assertThat(result.getFatigue()).isEqualTo(30);
        assertThat(result.getStress()).isEqualTo(20);
        assertThat(result.getHappiness()).isEqualTo(55);
        assertThat(result.getKnowledge()).isEqualTo(40);
        assertThat(result.getBurnout()).isFalse();
    }

    @DisplayName("게임 커리어를 저장하고 enum 필드를 문자열 매핑으로 조회할 수 있다.")
    @Test
    void saveAndLoadGameCareer() {
        // given
        GameCareer gameCareer = GameCareer.builder()
            .gameId(1001)
            .jobType(JobType.STARTUP)
            .jobTitle("사원")
            .salary(36_000_000)
            .tenureTurns(3)
            .recentStudyCount(2)
            .recentNetworkingCount(1)
            .negotiationPreparationScore(15)
            .lastNegotiatedTurn(0)
            .employmentStatus(EmploymentStatus.EMPLOYED)
            .probationEndTurn(6)
            .rehireAvailableTurn(null)
            .remainingUnemploymentBenefitTurns(0)
            .salaryBeforeResignation(null)
            .build();

        gameCareerRepository.save(gameCareer);

        // when
        GameCareer result = gameCareerRepository.findById(1001)
            .orElseThrow();

        // then
        assertThat(result.getJobType()).isEqualTo(JobType.STARTUP);
        assertThat(result.getEmploymentStatus()).isEqualTo(EmploymentStatus.EMPLOYED);
        assertThat(result.getProbationEndTurn()).isEqualTo(6);
    }

    @DisplayName("같은 턴 슬롯은 게임번호와 턴번호 기준으로 정렬 조회할 수 있다.")
    @Test
    void findTurnSlotsByGameIdAndTurnNumber() {
        // given
        gameTurnSlotRepository.save(GameTurnSlot.builder()
            .turnSlotId(2003)
            .gameId(1001)
            .turnNumber(5)
            .slotIndex(2)
            .actionType(ActionType.EXERCISE)
            .actionCategory(ActionCategory.ACTIVITY)
            .forcedAction(false)
            .build());
        gameTurnSlotRepository.save(GameTurnSlot.builder()
            .turnSlotId(2001)
            .gameId(1001)
            .turnNumber(5)
            .slotIndex(0)
            .actionType(ActionType.STUDY)
            .actionCategory(ActionCategory.ACTIVITY)
            .forcedAction(false)
            .build());
        gameTurnSlotRepository.save(GameTurnSlot.builder()
            .turnSlotId(2002)
            .gameId(1001)
            .turnNumber(5)
            .slotIndex(1)
            .actionType(ActionType.HOBBY)
            .actionCategory(ActionCategory.SHOPPING)
            .forcedAction(false)
            .build());

        // when
        List<GameTurnSlot> result = gameTurnSlotRepository.findAllByGameIdAndTurnNumberOrderBySlotIndex(1001, 5);

        // then
        assertThat(result).hasSize(3);
        assertThat(result)
            .extracting(GameTurnSlot::getActionType)
            .containsExactly(ActionType.STUDY, ActionType.HOBBY, ActionType.EXERCISE);
    }

    @DisplayName("게임 플레이 이력은 턴 순서대로 조회할 수 있다.")
    @Test
    void findGameplayHistoriesByGameId() {
        // given
        gameplayHistoryRepository.save(GameplayHistory.builder()
            .historyId(3002)
            .gameId(1001)
            .eventId(9001)
            .tableName("게임커리어")
            .columnName("연봉")
            .targetKey1("1001")
            .beforeValue("36000000")
            .afterValue("38000000")
            .startedAt(LocalDateTime.of(2026, 3, 1, 0, 0))
            .endedAt(LocalDateTime.of(2026, 3, 1, 0, 1))
            .effectPayload("{\"salaryChange\":2000000}")
            .summary("연봉 협상 결과 반영")
            .occurredTurn(2)
            .build());
        gameplayHistoryRepository.save(GameplayHistory.builder()
            .historyId(3001)
            .gameId(1001)
            .eventId(9000)
            .tableName("게임스탯")
            .columnName("지식")
            .targetKey1("1001")
            .beforeValue("35")
            .afterValue("43")
            .startedAt(LocalDateTime.of(2026, 2, 1, 0, 0))
            .endedAt(LocalDateTime.of(2026, 2, 1, 0, 1))
            .effectPayload("{\"knowledgeChange\":8}")
            .summary("공부 행동 결과 반영")
            .occurredTurn(1)
            .build());

        // when
        List<GameplayHistory> result = gameplayHistoryRepository.findAllByGameIdOrderByOccurredTurnAscHistoryIdAsc(1001);

        // then
        assertThat(result).hasSize(2);
        assertThat(result)
            .extracting(GameplayHistory::getOccurredTurn)
            .containsExactly(1, 2);
    }
}
