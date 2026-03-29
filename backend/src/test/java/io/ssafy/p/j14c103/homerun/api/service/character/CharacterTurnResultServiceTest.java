package io.ssafy.p.j14c103.homerun.api.service.character;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.api.service.character.request.CharacterTurnResultServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.character.request.CharacterTurnResultServiceRequest.TurnActionRequest;
import io.ssafy.p.j14c103.homerun.api.service.character.response.CharacterTurnResultServiceResponse;
import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.domain.character.GameStatRepository;
import io.ssafy.p.j14c103.homerun.domain.character.HealthRisk;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareer;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareerRepository;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.character.schedule.ActionCategory;
import io.ssafy.p.j14c103.homerun.domain.character.schedule.ActionType;
import io.ssafy.p.j14c103.homerun.domain.character.schedule.GameTurnSlot;
import io.ssafy.p.j14c103.homerun.domain.character.schedule.GameTurnSlotRepository;
import io.ssafy.p.j14c103.homerun.domain.history.GameplayHistory;
import io.ssafy.p.j14c103.homerun.domain.history.GameplayHistoryRepository;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CharacterTurnResultServiceTest {

    @Autowired
    private CharacterTurnResultService characterTurnResultService;

    @Autowired
    private GameStatRepository gameStatRepository;

    @Autowired
    private GameCareerRepository gameCareerRepository;

    @Autowired
    private GameplayHistoryRepository gameplayHistoryRepository;

    @Autowired
    private GameTurnSlotRepository gameTurnSlotRepository;

    @DisplayName("한 턴 결과를 반영하면 스탯, 커리어, 이력이 함께 갱신된다.")
    @Test
    void apply() {
        // given
        final CharacterTurnResultServiceRequest request =
            CharacterTurnResultServiceRequest.of(
                createWorkingCareer(2001, 5, EmploymentStatus.PROBATION, 10),
                createStableStat(2001),
                HousingType.VILLA,
                10,
                List.of(
                    TurnActionRequest.of(0, ActionType.STUDY),
                    TurnActionRequest.of(1, ActionType.EXERCISE),
                    TurnActionRequest.of(2, ActionType.REST)
                )
            );

        // when
        final CharacterTurnResultServiceResponse response = characterTurnResultService.apply(
            request
        );

        // then
        assertThat(response.getHealthRisk()).isEqualTo(HealthRisk.STABLE);
        assertThat(response.isForcedResigned()).isFalse();
        assertThat(response.isUnemploymentBenefitGranted()).isFalse();
        assertThat(response.getUnemploymentBenefitAmount()).isZero();
        assertThat(response.getStat().getHealth()).isEqualTo(79);
        assertThat(response.getStat().getFatigue()).isEqualTo(12);
        assertThat(response.getStat().getStress()).isEqualTo(3);
        assertThat(response.getStat().getHappiness()).isEqualTo(55);
        assertThat(response.getStat().getKnowledge()).isEqualTo(56);
        assertThat(response.getStat().isBurnout()).isFalse();
        assertThat(response.getCareer().getTenureTurns()).isEqualTo(6);
        assertThat(response.getCareer().getEmploymentStatus()).isEqualTo(
            EmploymentStatus.EMPLOYED
        );
        assertThat(response.getCareer().getProbationEndTurn()).isNull();
        assertThat(response.getCareer().getJobTitle()).isEqualTo("수습/인턴");

        final GameStat savedStat = gameStatRepository.findById(2001).orElseThrow();
        final GameCareer savedCareer = gameCareerRepository.findById(2001).orElseThrow();
        assertThat(savedStat.getKnowledge()).isEqualTo(56);
        assertThat(savedCareer.getEmploymentStatus()).isEqualTo(EmploymentStatus.EMPLOYED);
        assertThat(savedCareer.getTenureTurns()).isEqualTo(6);
        assertThat(savedCareer.getRecentStudyCount()).isEqualTo(1);
        assertThat(savedCareer.getRecentNetworkingCount()).isZero();
        assertThat(savedCareer.getNegotiationPreparationScore()).isEqualTo(4);

        final List<GameplayHistory> histories =
            gameplayHistoryRepository.findAllByGameIdOrderByOccurredTurnAscHistoryIdAsc(2001);
        assertThat(histories).hasSize(1);
        assertThat(histories.get(0).getTableName()).isEqualTo("게임스탯");
        assertThat(histories.get(0).getSummary()).isEqualTo("스탯 변경 결과를 반영했습니다.");
        assertThat(histories.get(0).getAfterValue()).contains("\"knowledge\":56");
    }

    @DisplayName("번아웃과 강제 퇴사가 동시에 필요하면 실업 급여 상태까지 함께 갱신한다.")
    @Test
    void applyWithBurnoutAndForcedResignation() {
        // given
        final CharacterTurnResultServiceRequest request =
            CharacterTurnResultServiceRequest.of(
                createWorkingCareer(2002, 10, EmploymentStatus.EMPLOYED, null),
                createRiskStat(2002),
                HousingType.NONE,
                15,
                List.of(
                    TurnActionRequest.of(0, ActionType.STUDY),
                    TurnActionRequest.of(1, ActionType.NETWORKING),
                    TurnActionRequest.of(2, ActionType.SIDE_JOB)
                )
            );

        // when
        final CharacterTurnResultServiceResponse response = characterTurnResultService.apply(
            request
        );

        // then
        assertThat(response.getHealthRisk()).isEqualTo(
            HealthRisk.FORCED_RESIGNATION_CANDIDATE
        );
        assertThat(response.isForcedResigned()).isTrue();
        assertThat(response.isUnemploymentBenefitGranted()).isTrue();
        assertThat(response.getUnemploymentBenefitAmount()).isEqualTo(1_500_000);
        assertThat(response.getStat().getHealth()).isEqualTo(5);
        assertThat(response.getStat().getFatigue()).isEqualTo(87);
        assertThat(response.getStat().getStress()).isEqualTo(100);
        assertThat(response.getStat().isBurnout()).isTrue();
        assertThat(response.getStat().getBurnoutStartedTurn()).isEqualTo(15);
        assertThat(response.getCareer().getEmploymentStatus()).isEqualTo(
            EmploymentStatus.UNEMPLOYED
        );
        assertThat(response.getCareer().getRehireAvailableTurn()).isEqualTo(17);
        assertThat(response.getCareer().getRemainingUnemploymentBenefitTurns()).isEqualTo(2);
        assertThat(response.getCareer().getSalaryBeforeResignation()).isEqualTo(36_000_000);

        final List<GameplayHistory> histories =
            gameplayHistoryRepository.findAllByGameIdOrderByOccurredTurnAscHistoryIdAsc(2002);
        assertThat(histories).hasSize(2);
        assertThat(histories.get(0).getTableName()).isEqualTo("게임커리어");
        assertThat(histories.get(0).getSummary()).isEqualTo("건강 악화로 강제 퇴사했습니다.");
        assertThat(histories.get(0).getAfterValue()).contains("\"employmentStatus\":\"UNEMPLOYED\"");
        assertThat(histories.get(0).getAfterValue())
            .contains("\"remainingUnemploymentBenefitTurns\":3");
        assertThat(histories.get(1).getTableName()).isEqualTo("게임스탯");
        assertThat(histories.get(1).getAfterValue()).contains("\"burnout\":true");
    }

    @DisplayName("실업 상태면 실업 급여를 소모하고 근속은 진행하지 않는다.")
    @Test
    void applyForUnemployedCareer() {
        // given
        final CharacterTurnResultServiceRequest request =
            CharacterTurnResultServiceRequest.of(
                createUnemployedCareer(2003),
                createUnemployedStat(2003),
                HousingType.STUDIO,
                20,
                List.of(
                    TurnActionRequest.of(0, ActionType.REST),
                    TurnActionRequest.of(1, ActionType.HOBBY),
                    TurnActionRequest.of(2, ActionType.REST)
                )
            );

        // when
        final CharacterTurnResultServiceResponse response = characterTurnResultService.apply(
            request
        );

        // then
        assertThat(response.getHealthRisk()).isEqualTo(HealthRisk.NEGOTIATION_PENALTY);
        assertThat(response.isForcedResigned()).isFalse();
        assertThat(response.isUnemploymentBenefitGranted()).isTrue();
        assertThat(response.getUnemploymentBenefitAmount()).isEqualTo(1_500_000);
        assertThat(response.getCareer().getEmploymentStatus()).isEqualTo(
            EmploymentStatus.UNEMPLOYED
        );
        assertThat(response.getCareer().getTenureTurns()).isEqualTo(10);
        assertThat(response.getCareer().getRemainingUnemploymentBenefitTurns()).isEqualTo(1);

        final List<GameplayHistory> histories =
            gameplayHistoryRepository.findAllByGameIdOrderByOccurredTurnAscHistoryIdAsc(2003);
        assertThat(histories).hasSize(1);
        assertThat(histories.get(0).getTableName()).isEqualTo("게임스탯");
    }

    @DisplayName("13턴부터는 12턴 전 행동을 제외하고 협상 준비도 카운터를 갱신한다.")
    @Test
    void applyWithNegotiationPreparationWindow() {
        // given
        gameTurnSlotRepository.save(createTurnSlot(3001, 2004, 1, 0, ActionType.STUDY));
        gameTurnSlotRepository.save(createTurnSlot(3002, 2004, 1, 1, ActionType.REST));
        gameTurnSlotRepository.save(createTurnSlot(3003, 2004, 1, 2, ActionType.HOBBY));

        final CharacterTurnResultServiceRequest request =
            CharacterTurnResultServiceRequest.of(
                createWorkingCareer(2004, 5, EmploymentStatus.EMPLOYED, null, 2, 1),
                createStableStat(2004),
                HousingType.VILLA,
                13,
                List.of(
                    TurnActionRequest.of(0, ActionType.STUDY),
                    TurnActionRequest.of(1, ActionType.NETWORKING),
                    TurnActionRequest.of(2, ActionType.REST)
                )
            );

        // when
        final CharacterTurnResultServiceResponse response = characterTurnResultService.apply(
            request
        );

        // then
        final GameCareer savedCareer = gameCareerRepository.findById(2004).orElseThrow();
        assertThat(savedCareer.getRecentStudyCount()).isEqualTo(2);
        assertThat(savedCareer.getRecentNetworkingCount()).isEqualTo(2);
        assertThat(savedCareer.getNegotiationPreparationScore()).isEqualTo(12);
    }

    @DisplayName("위기 사이클이면 턴 결과 반영 시 강제 퇴사 재취업 대기 턴이 늘어난다.")
    @Test
    void applyWithCyclePenalty() {
        // given
        final CharacterTurnResultServiceRequest request =
            CharacterTurnResultServiceRequest.of(
                createWorkingCareer(2005, 10, EmploymentStatus.EMPLOYED, null),
                createRiskStat(2005),
                HousingType.NONE,
                15,
                List.of(
                    TurnActionRequest.of(0, ActionType.STUDY),
                    TurnActionRequest.of(1, ActionType.NETWORKING),
                    TurnActionRequest.of(2, ActionType.SIDE_JOB)
                ),
                CyclePhase.CRISIS
            );

        // when
        final CharacterTurnResultServiceResponse response = characterTurnResultService.apply(
            request
        );

        // then
        assertThat(response.isForcedResigned()).isTrue();
        assertThat(response.getCareer().getRehireAvailableTurn()).isEqualTo(19);
    }

    private GameCareer createWorkingCareer(
        final int gameId,
        final int tenureTurns,
        final EmploymentStatus employmentStatus,
        final Integer probationEndTurn
    ) {
        return createWorkingCareer(gameId, tenureTurns, employmentStatus, probationEndTurn, 0, 0);
    }

    private GameCareer createWorkingCareer(
        final int gameId,
        final int tenureTurns,
        final EmploymentStatus employmentStatus,
        final Integer probationEndTurn,
        final int recentStudyCount,
        final int recentNetworkingCount
    ) {
        return GameCareer.builder()
            .gameId(gameId)
            .jobType(JobType.STARTUP)
            .jobTitle("수습/인턴")
            .salary(36_000_000)
            .tenureTurns(tenureTurns)
            .recentStudyCount(recentStudyCount)
            .recentNetworkingCount(recentNetworkingCount)
            .negotiationPreparationScore(0)
            .lastNegotiatedTurn(0)
            .employmentStatus(employmentStatus)
            .probationEndTurn(probationEndTurn)
            .rehireAvailableTurn(null)
            .remainingUnemploymentBenefitTurns(0)
            .salaryBeforeResignation(null)
            .build();
    }

    private GameCareer createUnemployedCareer(final int gameId) {
        return GameCareer.builder()
            .gameId(gameId)
            .jobType(JobType.MID_BIZ)
            .jobTitle("사원")
            .salary(36_000_000)
            .tenureTurns(10)
            .recentStudyCount(0)
            .recentNetworkingCount(0)
            .negotiationPreparationScore(0)
            .lastNegotiatedTurn(0)
            .employmentStatus(EmploymentStatus.UNEMPLOYED)
            .probationEndTurn(null)
            .rehireAvailableTurn(21)
            .remainingUnemploymentBenefitTurns(2)
            .salaryBeforeResignation(36_000_000)
            .build();
    }

    private GameStat createStableStat(final int gameId) {
        return GameStat.builder()
            .gameId(gameId)
            .health(70)
            .fatigue(20)
            .stress(20)
            .happiness(50)
            .knowledge(50)
            .burnout(false)
            .burnoutStartedTurn(null)
            .hospitalizedUntilTurn(null)
            .build();
    }

    private GameStat createRiskStat(final int gameId) {
        return GameStat.builder()
            .gameId(gameId)
            .health(12)
            .fatigue(70)
            .stress(75)
            .happiness(0)
            .knowledge(30)
            .burnout(false)
            .burnoutStartedTurn(null)
            .hospitalizedUntilTurn(null)
            .build();
    }

    private GameStat createUnemployedStat(final int gameId) {
        return GameStat.builder()
            .gameId(gameId)
            .health(65)
            .fatigue(30)
            .stress(30)
            .happiness(40)
            .knowledge(45)
            .burnout(false)
            .burnoutStartedTurn(null)
            .hospitalizedUntilTurn(null)
            .build();
    }

    private GameTurnSlot createTurnSlot(
        final int turnSlotId,
        final int gameId,
        final int turnNumber,
        final int slotIndex,
        final ActionType actionType
    ) {
        return GameTurnSlot.builder()
            .turnSlotId(turnSlotId)
            .gameId(gameId)
            .turnNumber(turnNumber)
            .slotIndex(slotIndex)
            .actionType(actionType)
            .actionCategory(actionType == ActionType.HOBBY
                ? ActionCategory.SHOPPING
                : ActionCategory.ACTIVITY)
            .forcedAction(false)
            .build();
    }
}
