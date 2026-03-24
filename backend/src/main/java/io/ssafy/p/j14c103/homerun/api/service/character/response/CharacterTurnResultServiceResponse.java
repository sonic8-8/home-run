package io.ssafy.p.j14c103.homerun.api.service.character.response;

import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.domain.character.HealthRisk;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareer;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CharacterTurnResultServiceResponse {

    private final HealthRisk healthRisk;
    private final boolean forcedResigned;
    private final boolean unemploymentBenefitGranted;
    private final int unemploymentBenefitAmount;
    private final StatResultResponse stat;
    private final CareerResultResponse career;

    @Builder(access = AccessLevel.PRIVATE)
    private CharacterTurnResultServiceResponse(
        final HealthRisk healthRisk,
        final boolean forcedResigned,
        final boolean unemploymentBenefitGranted,
        final int unemploymentBenefitAmount,
        final StatResultResponse stat,
        final CareerResultResponse career
    ) {
        validateRequest(
            healthRisk,
            unemploymentBenefitGranted,
            unemploymentBenefitAmount,
            stat,
            career
        );

        this.healthRisk = healthRisk;
        this.forcedResigned = forcedResigned;
        this.unemploymentBenefitGranted = unemploymentBenefitGranted;
        this.unemploymentBenefitAmount = unemploymentBenefitAmount;
        this.stat = stat;
        this.career = career;
    }

    public static CharacterTurnResultServiceResponse of(
        final HealthRisk healthRisk,
        final boolean forcedResigned,
        final boolean unemploymentBenefitGranted,
        final int unemploymentBenefitAmount,
        final GameStat gameStat,
        final GameCareer gameCareer
    ) {
        return CharacterTurnResultServiceResponse.builder()
            .healthRisk(healthRisk)
            .forcedResigned(forcedResigned)
            .unemploymentBenefitGranted(unemploymentBenefitGranted)
            .unemploymentBenefitAmount(unemploymentBenefitAmount)
            .stat(StatResultResponse.from(gameStat))
            .career(CareerResultResponse.from(gameCareer))
            .build();
    }

    private void validateRequest(
        final HealthRisk healthRisk,
        final boolean unemploymentBenefitGranted,
        final int unemploymentBenefitAmount,
        final StatResultResponse stat,
        final CareerResultResponse career
    ) {
        if (healthRisk == null || stat == null || career == null) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
        if (unemploymentBenefitAmount < 0) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
        if (unemploymentBenefitGranted && unemploymentBenefitAmount == 0) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
        if (!unemploymentBenefitGranted && unemploymentBenefitAmount != 0) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
    }

    @Getter
    public static class StatResultResponse {

        private final int health;
        private final int fatigue;
        private final int stress;
        private final int happiness;
        private final int knowledge;
        private final boolean burnout;
        private final Integer burnoutStartedTurn;
        private final Integer hospitalizedUntilTurn;

        @Builder(access = AccessLevel.PRIVATE)
        private StatResultResponse(
            final int health,
            final int fatigue,
            final int stress,
            final int happiness,
            final int knowledge,
            final boolean burnout,
            final Integer burnoutStartedTurn,
            final Integer hospitalizedUntilTurn
        ) {
            validateStat(health);
            validateStat(fatigue);
            validateStat(stress);
            validateStat(happiness);
            validateStat(knowledge);

            this.health = health;
            this.fatigue = fatigue;
            this.stress = stress;
            this.happiness = happiness;
            this.knowledge = knowledge;
            this.burnout = burnout;
            this.burnoutStartedTurn = burnoutStartedTurn;
            this.hospitalizedUntilTurn = hospitalizedUntilTurn;
        }

        public static StatResultResponse from(final GameStat gameStat) {
            if (gameStat == null) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }

            return StatResultResponse.builder()
                .health(gameStat.getHealth())
                .fatigue(gameStat.getFatigue())
                .stress(gameStat.getStress())
                .happiness(gameStat.getHappiness())
                .knowledge(gameStat.getKnowledge())
                .burnout(Boolean.TRUE.equals(gameStat.getBurnout()))
                .burnoutStartedTurn(gameStat.getBurnoutStartedTurn())
                .hospitalizedUntilTurn(gameStat.getHospitalizedUntilTurn())
                .build();
        }

        private static void validateStat(final int stat) {
            if (stat < 0 || stat > 100) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }
        }
    }

    @Getter
    public static class CareerResultResponse {

        private final int gameId;
        private final JobType jobType;
        private final String jobTitle;
        private final int salary;
        private final int tenureTurns;
        private final EmploymentStatus employmentStatus;
        private final Integer probationEndTurn;
        private final Integer rehireAvailableTurn;
        private final int remainingUnemploymentBenefitTurns;
        private final Integer salaryBeforeResignation;

        @Builder(access = AccessLevel.PRIVATE)
        private CareerResultResponse(
            final int gameId,
            final JobType jobType,
            final String jobTitle,
            final int salary,
            final int tenureTurns,
            final EmploymentStatus employmentStatus,
            final Integer probationEndTurn,
            final Integer rehireAvailableTurn,
            final int remainingUnemploymentBenefitTurns,
            final Integer salaryBeforeResignation
        ) {
            if (gameId < 1 || jobType == null || employmentStatus == null) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }
            if (jobTitle == null || jobTitle.isBlank()) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }
            if (salary < 0 || tenureTurns < 0 || remainingUnemploymentBenefitTurns < 0) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }

            this.gameId = gameId;
            this.jobType = jobType;
            this.jobTitle = jobTitle;
            this.salary = salary;
            this.tenureTurns = tenureTurns;
            this.employmentStatus = employmentStatus;
            this.probationEndTurn = probationEndTurn;
            this.rehireAvailableTurn = rehireAvailableTurn;
            this.remainingUnemploymentBenefitTurns = remainingUnemploymentBenefitTurns;
            this.salaryBeforeResignation = salaryBeforeResignation;
        }

        public static CareerResultResponse from(final GameCareer gameCareer) {
            if (gameCareer == null) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }

            return CareerResultResponse.builder()
                .gameId(gameCareer.getGameId())
                .jobType(gameCareer.getJobType())
                .jobTitle(gameCareer.getJobTitle())
                .salary(gameCareer.getSalary())
                .tenureTurns(gameCareer.getTenureTurns())
                .employmentStatus(gameCareer.getEmploymentStatus())
                .probationEndTurn(gameCareer.getProbationEndTurn())
                .rehireAvailableTurn(gameCareer.getRehireAvailableTurn())
                .remainingUnemploymentBenefitTurns(
                    gameCareer.getRemainingUnemploymentBenefitTurns()
                )
                .salaryBeforeResignation(gameCareer.getSalaryBeforeResignation())
                .build();
        }
    }
}
