package io.ssafy.p.j14c103.homerun.api.service.character;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import io.ssafy.p.j14c103.homerun.api.service.character.career.JobOfferQueryService;
import io.ssafy.p.j14c103.homerun.api.service.character.career.JobTransferService;
import io.ssafy.p.j14c103.homerun.api.service.character.career.SalaryNegotiationService;
import io.ssafy.p.j14c103.homerun.api.service.character.career.request.JobOfferQueryRequest;
import io.ssafy.p.j14c103.homerun.api.service.character.career.request.JobTransferServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.character.career.request.SalaryNegotiationRequest;
import io.ssafy.p.j14c103.homerun.api.service.character.career.response.JobOfferQueryResponse;
import io.ssafy.p.j14c103.homerun.api.service.character.career.response.JobTransferServiceResponse;
import io.ssafy.p.j14c103.homerun.api.service.character.career.response.SalaryNegotiationResultResponse;
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
import io.ssafy.p.j14c103.homerun.domain.character.career.SalaryNegotiationPolicy;
import io.ssafy.p.j14c103.homerun.domain.character.schedule.ActionCategory;
import io.ssafy.p.j14c103.homerun.domain.character.schedule.ActionType;
import io.ssafy.p.j14c103.homerun.domain.character.schedule.GameTurnSlot;
import io.ssafy.p.j14c103.homerun.domain.character.schedule.GameTurnSlotRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
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
class CharacterLongPlayRegressionTest {

    private static final List<ActionType> STABLE_ACTIONS = List.of(
        ActionType.STUDY,
        ActionType.EXERCISE,
        ActionType.REST
    );
    private static final List<ActionType> OVERLOAD_ACTIONS = List.of(
        ActionType.SIDE_JOB,
        ActionType.SIDE_JOB,
        ActionType.SIDE_JOB
    );
    private static final List<ActionType> FORCED_RESIGNATION_TRIGGER_ACTIONS = List.of(
        ActionType.STUDY,
        ActionType.NETWORKING,
        ActionType.SIDE_JOB
    );
    private static final List<ActionType> BURNOUT_RECOVERY_ACTIONS = List.of(
        ActionType.REST,
        ActionType.REST,
        ActionType.REST
    );
    private static final int LONG_PLAY_TURNS = 18;
    private static final int REHIRE_AVAILABLE_TURN = 18;

    @Autowired
    private CharacterTurnResultService characterTurnResultService;

    @Autowired
    private SalaryNegotiationService salaryNegotiationService;

    @Autowired
    private JobOfferQueryService jobOfferQueryService;

    @Autowired
    private JobTransferService jobTransferService;

    @Autowired
    private GameStatRepository gameStatRepository;

    @Autowired
    private GameCareerRepository gameCareerRepository;

    @Autowired
    private GameTurnSlotRepository gameTurnSlotRepository;

    @DisplayName("18턴 장기 플레이에서도 스탯 범위와 커리어 불변식이 유지된다.")
    @Test
    void longPlayKeepsStatAndCareerContractsIntact() {
        // given
        final int gameId = 92001;
        final GameCareer gameCareer = gameCareerRepository.saveAndFlush(createCareer(
            gameId,
            JobType.STARTUP,
            36_000_000,
            0,
            EmploymentStatus.PROBATION,
            6,
            "수습/인턴"
        ));
        final GameStat gameStat = gameStatRepository.saveAndFlush(
            GameStat.create(gameId, 70, 20, 20, 50, 50, 0)
        );

        CharacterTurnResultServiceResponse lastResponse = null;

        // when
        for (int turn = 1; turn <= LONG_PLAY_TURNS; turn++) {
            lastResponse = playTurn(
                gameCareer,
                gameStat,
                HousingType.VILLA,
                turn,
                STABLE_ACTIONS
            );

            assertStatRange(lastResponse.getStat(), turn);
            assertCareerInvariant(lastResponse.getCareer());
            assertThat(gameCareer.getRecentStudyCount()).isBetween(0, 12);
            assertThat(gameCareer.getRecentNetworkingCount()).isBetween(0, 12);
        }

        // then
        assertThat(lastResponse).isNotNull();
        assertThat(lastResponse.getHealthRisk()).isEqualTo(HealthRisk.STABLE);
        assertThat(lastResponse.isForcedResigned()).isFalse();
        assertThat(lastResponse.getCareer().getEmploymentStatus()).isEqualTo(
            EmploymentStatus.EMPLOYED
        );
        assertThat(lastResponse.getCareer().getProbationEndTurn()).isNull();
        assertThat(lastResponse.getCareer().getTenureTurns()).isEqualTo(LONG_PLAY_TURNS);
        assertThat(gameCareer.getRecentStudyCount()).isEqualTo(12);
        assertThat(gameCareer.getRecentNetworkingCount()).isZero();
        assertThat(gameCareer.getNegotiationPreparationScore()).isEqualTo(29);
        assertThat(lastResponse.getStat().isBurnout()).isFalse();
        assertThat(lastResponse.getStat().getBurnoutStartedTurn()).isNull();
    }

    @DisplayName("과부하와 회복이 반복돼도 번아웃은 해제 후 다시 정상적으로 재발한다.")
    @Test
    void longPlaySupportsBurnoutRecoveryAndReentry() {
        // given
        final int gameId = 92002;
        final GameCareer gameCareer = gameCareerRepository.saveAndFlush(createCareer(
            gameId,
            JobType.SMALL_BIZ,
            30_000_000,
            10,
            EmploymentStatus.EMPLOYED,
            null,
            "사원"
        ));
        final GameStat gameStat = gameStatRepository.saveAndFlush(
            GameStat.create(gameId, 90, 54, 41, 0, 20, 0)
        );

        // when
        final CharacterTurnResultServiceResponse firstOverload = playTurn(
            gameCareer,
            gameStat,
            HousingType.NONE,
            1,
            OVERLOAD_ACTIONS
        );
        final CharacterTurnResultServiceResponse recovery = playTurn(
            gameCareer,
            gameStat,
            HousingType.OWNED_APT,
            2,
            BURNOUT_RECOVERY_ACTIONS
        );
        final CharacterTurnResultServiceResponse secondOverload = playTurn(
            gameCareer,
            gameStat,
            HousingType.NONE,
            3,
            OVERLOAD_ACTIONS
        );
        final CharacterTurnResultServiceResponse burnoutAgain = playTurn(
            gameCareer,
            gameStat,
            HousingType.NONE,
            4,
            OVERLOAD_ACTIONS
        );

        // then
        assertThat(firstOverload.getStat().isBurnout()).isTrue();
        assertThat(firstOverload.getStat().getBurnoutStartedTurn()).isEqualTo(1);
        assertThat(recovery.getStat().isBurnout()).isFalse();
        assertThat(recovery.getStat().getBurnoutStartedTurn()).isNull();
        assertThat(secondOverload.getStat().isBurnout()).isFalse();
        assertThat(secondOverload.getStat().getBurnoutStartedTurn()).isNull();
        assertThat(burnoutAgain.getStat().isBurnout()).isTrue();
        assertThat(burnoutAgain.getStat().getBurnoutStartedTurn()).isEqualTo(4);
        assertThat(burnoutAgain.isForcedResigned()).isFalse();

        assertStatRange(firstOverload.getStat(), 1);
        assertStatRange(recovery.getStat(), 2);
        assertStatRange(secondOverload.getStat(), 3);
        assertStatRange(burnoutAgain.getStat(), 4);
        assertCareerInvariant(burnoutAgain.getCareer());
    }

    @DisplayName("장기 플레이 이후 협상, 이직, 강제 퇴사, 실업 상태 전이가 일관되게 유지된다.")
    @Test
    void longPlayCareerTransitionsRemainCoherent() {
        // given
        final int gameId = 92003;
        final GameCareer gameCareer = gameCareerRepository.saveAndFlush(createCareer(
            gameId,
            JobType.SMALL_BIZ,
            30_000_000,
            0,
            EmploymentStatus.PROBATION,
            6,
            "수습/인턴"
        ));
        final GameStat gameStat = gameStatRepository.saveAndFlush(
            GameStat.create(gameId, 70, 20, 20, 50, 50, 0)
        );

        for (int turn = 1; turn <= 12; turn++) {
            playTurn(gameCareer, gameStat, HousingType.VILLA, turn, STABLE_ACTIONS);
        }

        // when
        final SalaryNegotiationResultResponse negotiationResponse =
            salaryNegotiationService.negotiate(
                SalaryNegotiationRequest.of(gameCareer, gameStat, 13)
            );
        applyNegotiationResult(gameCareer, negotiationResponse);
        gameCareerRepository.saveAndFlush(gameCareer);

        final JobOfferQueryResponse offerQueryResponse = jobOfferQueryService.getJobOffers(
            JobOfferQueryRequest.of(gameCareer, gameStat, 2)
        );
        final int expectedTransferSalary = offerQueryResponse.offers().stream()
            .filter(offer -> "OFFER-004".equals(offer.offerId()))
            .findFirst()
            .orElseThrow()
            .offeredSalary();
        final int expectedUnemploymentBenefit = calculateExpectedUnemploymentBenefit(
            expectedTransferSalary
        );
        final JobTransferServiceResponse transferResponse = jobTransferService.transfer(
            JobTransferServiceRequest.of(gameCareer, gameStat, 2, "OFFER-004", 14)
        );
        gameCareerRepository.saveAndFlush(gameCareer);

        playTurn(gameCareer, gameStat, HousingType.VILLA, 15, STABLE_ACTIONS);
        final CharacterTurnResultServiceResponse releasedProbation = playTurn(
            gameCareer,
            gameStat,
            HousingType.VILLA,
            16,
            STABLE_ACTIONS
        );

        overwriteStat(gameStat, 12, 70, 75, 0, 100, 16);
        gameStatRepository.saveAndFlush(gameStat);

        final CharacterTurnResultServiceResponse forcedResignation = playTurn(
            gameCareer,
            gameStat,
            HousingType.NONE,
            17,
            FORCED_RESIGNATION_TRIGGER_ACTIONS
        );
        final CharacterTurnResultServiceResponse unemployedTurn18 = playTurn(
            gameCareer,
            gameStat,
            HousingType.OWNED_APT,
            18,
            BURNOUT_RECOVERY_ACTIONS
        );
        final CharacterTurnResultServiceResponse unemployedTurn19 = playTurn(
            gameCareer,
            gameStat,
            HousingType.OWNED_APT,
            19,
            BURNOUT_RECOVERY_ACTIONS
        );
        final JobOfferQueryResponse unemployedOffers = jobOfferQueryService.getJobOffers(
            JobOfferQueryRequest.of(gameCareer, gameStat, 2)
        );

        // then
        assertThat(negotiationResponse.previousSalary()).isEqualTo(30_000_000);
        assertThat(negotiationResponse.newSalary()).isEqualTo(33_000_000);
        assertThat(negotiationResponse.raiseRate()).isEqualTo(10);
        assertThat(negotiationResponse.lastNegotiatedTurn()).isEqualTo(13);

        assertThatThrownBy(() -> salaryNegotiationService.negotiate(
            SalaryNegotiationRequest.of(gameCareer, gameStat, 24)
        ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.CHARACTER_REQUEST_INVALID);

        assertThat(offerQueryResponse.offerChanceBonusRate()).isEqualTo(10);
        assertThat(offerQueryResponse.meetFriendBonusApplied()).isTrue();
        assertThat(offerQueryResponse.offers())
            .extracting(
                JobOfferQueryResponse.JobOfferResponse::offerId,
                JobOfferQueryResponse.JobOfferResponse::jobType,
                JobOfferQueryResponse.JobOfferResponse::offeredSalary
            )
            .contains(tuple("OFFER-004", JobType.LARGE_BIZ, expectedTransferSalary));

        assertThat(transferResponse.previousJobType()).isEqualTo(JobType.SMALL_BIZ);
        assertThat(transferResponse.newJobType()).isEqualTo(JobType.LARGE_BIZ);
        assertThat(transferResponse.newJobTitle()).isEqualTo("수습/인턴");
        assertThat(transferResponse.newSalary()).isEqualTo(expectedTransferSalary);
        assertThat(transferResponse.probationEndTurn()).isEqualTo(16);
        assertThat(transferResponse.tenureReset()).isTrue();

        assertThat(releasedProbation.getCareer().getEmploymentStatus()).isEqualTo(
            EmploymentStatus.EMPLOYED
        );
        assertThat(releasedProbation.getCareer().getProbationEndTurn()).isNull();
        assertThat(releasedProbation.getCareer().getTenureTurns()).isEqualTo(2);

        assertThat(forcedResignation.isForcedResigned()).isTrue();
        assertThat(forcedResignation.getHealthRisk()).isEqualTo(
            HealthRisk.FORCED_RESIGNATION_CANDIDATE
        );
        assertThat(forcedResignation.isUnemploymentBenefitGranted()).isTrue();
        assertThat(forcedResignation.getUnemploymentBenefitAmount())
            .isEqualTo(expectedUnemploymentBenefit);
        assertThat(forcedResignation.getCareer().getEmploymentStatus()).isEqualTo(
            EmploymentStatus.UNEMPLOYED
        );
        assertThat(forcedResignation.getCareer().getProbationEndTurn()).isNull();
        assertThat(forcedResignation.getCareer().getRehireAvailableTurn()).isEqualTo(
            REHIRE_AVAILABLE_TURN
        );
        assertThat(forcedResignation.getCareer().getRemainingUnemploymentBenefitTurns())
            .isEqualTo(2);
        assertThat(forcedResignation.getCareer().getSalaryBeforeResignation())
            .isEqualTo(expectedTransferSalary);
        assertThat(forcedResignation.getCareer().getTenureTurns()).isEqualTo(3);

        assertThat(unemployedTurn18.isForcedResigned()).isFalse();
        assertThat(unemployedTurn18.isUnemploymentBenefitGranted()).isTrue();
        assertThat(unemployedTurn18.getUnemploymentBenefitAmount())
            .isEqualTo(expectedUnemploymentBenefit);
        assertThat(unemployedTurn18.getCareer().getTenureTurns()).isEqualTo(3);
        assertThat(unemployedTurn18.getCareer().getRehireAvailableTurn()).isEqualTo(
            REHIRE_AVAILABLE_TURN
        );
        assertThat(unemployedTurn18.getCareer().getRemainingUnemploymentBenefitTurns())
            .isEqualTo(1);

        assertThat(unemployedTurn19.isForcedResigned()).isFalse();
        assertThat(unemployedTurn19.isUnemploymentBenefitGranted()).isTrue();
        assertThat(unemployedTurn19.getUnemploymentBenefitAmount())
            .isEqualTo(expectedUnemploymentBenefit);
        assertThat(unemployedTurn19.getCareer().getTenureTurns()).isEqualTo(3);
        assertThat(unemployedTurn19.getCareer().getRehireAvailableTurn()).isEqualTo(
            REHIRE_AVAILABLE_TURN
        );
        assertThat(unemployedTurn19.getCareer().getRemainingUnemploymentBenefitTurns())
            .isZero();
        assertThat(unemployedOffers.offers()).isEmpty();

        assertStatRange(unemployedTurn19.getStat(), 19);
        assertCareerInvariant(unemployedTurn19.getCareer());
    }

    private CharacterTurnResultServiceResponse playTurn(
        final GameCareer gameCareer,
        final GameStat gameStat,
        final HousingType housingType,
        final int currentTurn,
        final List<ActionType> actionTypes
    ) {
        saveTurnSlots(gameCareer.getGameId(), currentTurn, actionTypes);

        return characterTurnResultService.apply(
            CharacterTurnResultServiceRequest.of(
                gameCareer,
                gameStat,
                housingType,
                currentTurn,
                toTurnActionRequests(actionTypes)
            )
        );
    }

    private void saveTurnSlots(
        final Integer gameId,
        final int currentTurn,
        final List<ActionType> actionTypes
    ) {
        final List<GameTurnSlot> turnSlots = java.util.stream.IntStream.range(0, actionTypes.size())
            .mapToObj(slotIndex -> GameTurnSlot.builder()
                .turnSlotId(gameId * 1_000 + currentTurn * 10 + slotIndex)
                .gameId(gameId)
                .turnNumber(currentTurn)
                .slotIndex(slotIndex)
                .actionType(actionTypes.get(slotIndex))
                .actionCategory(resolveActionCategory(actionTypes.get(slotIndex)))
                .forcedAction(false)
                .build())
            .toList();

        gameTurnSlotRepository.saveAllAndFlush(turnSlots);
    }

    private List<TurnActionRequest> toTurnActionRequests(final List<ActionType> actionTypes) {
        return java.util.stream.IntStream.range(0, actionTypes.size())
            .mapToObj(slotIndex -> TurnActionRequest.of(slotIndex, actionTypes.get(slotIndex)))
            .toList();
    }

    private ActionCategory resolveActionCategory(final ActionType actionType) {
        if (actionType == ActionType.HOBBY || actionType == ActionType.MEET_FRIEND) {
            return ActionCategory.SHOPPING;
        }

        return ActionCategory.ACTIVITY;
    }

    private void applyNegotiationResult(
        final GameCareer gameCareer,
        final SalaryNegotiationResultResponse response
    ) {
        gameCareer.applySalaryNegotiation(
            SalaryNegotiationPolicy.NegotiationResult.of(
                response.previousSalary(),
                response.newSalary(),
                response.raiseRate(),
                response.lastNegotiatedTurn(),
                response.message()
            )
        );
    }

    private void overwriteStat(
        final GameStat gameStat,
        final int health,
        final int fatigue,
        final int stress,
        final int happiness,
        final int knowledge,
        final int currentTurn
    ) {
        gameStat.applyChange(
            health - gameStat.getHealth(),
            fatigue - gameStat.getFatigue(),
            stress - gameStat.getStress(),
            happiness - gameStat.getHappiness(),
            knowledge - gameStat.getKnowledge(),
            currentTurn
        );
    }

    private int calculateExpectedUnemploymentBenefit(final int annualSalary) {
        return annualSalary / 24;
    }

    private void assertStatRange(
        final CharacterTurnResultServiceResponse.StatResultResponse stat,
        final int currentTurn
    ) {
        assertThat(stat.getHealth())
            .as("turn %s health", currentTurn)
            .isBetween(0, 100);
        assertThat(stat.getFatigue())
            .as("turn %s fatigue", currentTurn)
            .isBetween(0, 100);
        assertThat(stat.getStress())
            .as("turn %s stress", currentTurn)
            .isBetween(0, 100);
        assertThat(stat.getHappiness())
            .as("turn %s happiness", currentTurn)
            .isBetween(0, 100);
        assertThat(stat.getKnowledge())
            .as("turn %s knowledge", currentTurn)
            .isBetween(0, 100);

        if (stat.isBurnout()) {
            assertThat(stat.getBurnoutStartedTurn()).isNotNull();
            assertThat(stat.getBurnoutStartedTurn()).isLessThanOrEqualTo(currentTurn);
            return;
        }

        assertThat(stat.getBurnoutStartedTurn()).isNull();
    }

    private void assertCareerInvariant(
        final CharacterTurnResultServiceResponse.CareerResultResponse career
    ) {
        assertThat(career.getSalary()).isPositive();
        assertThat(career.getTenureTurns()).isNotNegative();
        assertThat(career.getRemainingUnemploymentBenefitTurns()).isNotNegative();
        assertThat(career.getEmploymentStatus()).isNotNull();
        assertThat(career.getJobType()).isNotNull();
        assertThat(career.getJobTitle()).isNotBlank();

        if (career.getEmploymentStatus() == EmploymentStatus.UNEMPLOYED) {
            assertThat(career.getProbationEndTurn()).isNull();
            assertThat(career.getRehireAvailableTurn()).isNotNull();
            assertThat(career.getSalaryBeforeResignation()).isNotNull();
            return;
        }

        assertThat(career.getRehireAvailableTurn()).isNull();
        assertThat(career.getSalaryBeforeResignation()).isNull();

        if (career.getEmploymentStatus() == EmploymentStatus.PROBATION) {
            assertThat(career.getProbationEndTurn()).isNotNull();
            return;
        }

        assertThat(career.getProbationEndTurn()).isNull();
    }

    private GameCareer createCareer(
        final int gameId,
        final JobType jobType,
        final int salary,
        final int tenureTurns,
        final EmploymentStatus employmentStatus,
        final Integer probationEndTurn,
        final String jobTitle
    ) {
        return GameCareer.builder()
            .gameId(gameId)
            .jobType(jobType)
            .jobTitle(jobTitle)
            .salary(salary)
            .tenureTurns(tenureTurns)
            .recentStudyCount(0)
            .recentNetworkingCount(0)
            .negotiationPreparationScore(0)
            .lastNegotiatedTurn(0)
            .employmentStatus(employmentStatus)
            .probationEndTurn(probationEndTurn)
            .rehireAvailableTurn(null)
            .remainingUnemploymentBenefitTurns(0)
            .salaryBeforeResignation(null)
            .build();
    }
}
