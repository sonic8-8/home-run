package io.ssafy.p.j14c103.homerun.api.service.character.response;

import io.ssafy.p.j14c103.homerun.domain.character.CharacterSeedPolicy;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CharacterSeedResponse {

    private final CharacterType characterType;
    private final SessionSeedResponse session;
    private final StatSeedResponse stat;
    private final CareerSeedResponse career;

    @Builder(access = AccessLevel.PRIVATE)
    private CharacterSeedResponse(
        final CharacterType characterType,
        final SessionSeedResponse session,
        final StatSeedResponse stat,
        final CareerSeedResponse career
    ) {
        validateRequest(characterType, session, stat, career);

        this.characterType = characterType;
        this.session = session;
        this.stat = stat;
        this.career = career;
    }

    public static CharacterSeedResponse from(final CharacterSeedPolicy.CharacterSeedPlan plan) {
        return CharacterSeedResponse.builder()
            .characterType(plan.characterType())
            .session(SessionSeedResponse.from(plan.session()))
            .stat(StatSeedResponse.from(plan.stat()))
            .career(CareerSeedResponse.from(plan.career()))
            .build();
    }

    private void validateRequest(
        final CharacterType characterType,
        final SessionSeedResponse session,
        final StatSeedResponse stat,
        final CareerSeedResponse career
    ) {
        if (characterType == null || session == null || stat == null || career == null) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
    }

    @Getter
    public static class SessionSeedResponse {

        private final JobType jobTypeSummary;
        private final String seedType;
        private final int initialCash;
        private final int initialNetAssets;

        @Builder(access = AccessLevel.PRIVATE)
        private SessionSeedResponse(
            final JobType jobTypeSummary,
            final String seedType,
            final int initialCash,
            final int initialNetAssets
        ) {
            validateRequest(jobTypeSummary, seedType, initialCash, initialNetAssets);

            this.jobTypeSummary = jobTypeSummary;
            this.seedType = seedType;
            this.initialCash = initialCash;
            this.initialNetAssets = initialNetAssets;
        }

        public static SessionSeedResponse from(final CharacterSeedPolicy.SessionSeed sessionSeed) {
            return SessionSeedResponse.builder()
                .jobTypeSummary(sessionSeed.jobTypeSummary())
                .seedType(sessionSeed.seedType().name())
                .initialCash(sessionSeed.initialCash())
                .initialNetAssets(sessionSeed.initialNetAssets())
                .build();
        }

        private void validateRequest(
            final JobType jobTypeSummary,
            final String seedType,
            final int initialCash,
            final int initialNetAssets
        ) {
            if (jobTypeSummary == null) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }
            if (seedType == null || seedType.isBlank()) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }
            validateNonNegative(initialCash);
            validateNonNegative(initialNetAssets);
        }
    }

    @Getter
    public static class StatSeedResponse {

        private final int health;
        private final int fatigue;
        private final int stress;
        private final int knowledge;
        private final int happiness;

        @Builder(access = AccessLevel.PRIVATE)
        private StatSeedResponse(
            final int health,
            final int fatigue,
            final int stress,
            final int knowledge,
            final int happiness
        ) {
            validateRange(health);
            validateRange(fatigue);
            validateRange(stress);
            validateRange(knowledge);
            validateRange(happiness);

            this.health = health;
            this.fatigue = fatigue;
            this.stress = stress;
            this.knowledge = knowledge;
            this.happiness = happiness;
        }

        public static StatSeedResponse from(final CharacterSeedPolicy.StatSeed statSeed) {
            return StatSeedResponse.builder()
                .health(statSeed.health())
                .fatigue(statSeed.fatigue())
                .stress(statSeed.stress())
                .knowledge(statSeed.knowledge())
                .happiness(statSeed.happiness())
                .build();
        }
    }

    @Getter
    public static class CareerSeedResponse {

        private final JobType jobType;
        private final String jobTitle;
        private final int annualSalary;
        private final int monthlySalary;
        private final int tenureTurns;
        private final int recentStudyCount;
        private final int recentNetworkingCount;
        private final int negotiationPreparationScore;
        private final int lastNegotiatedTurn;
        private final EmploymentStatus employmentStatus;
        private final Integer probationEndTurn;
        private final Integer rehireAvailableTurn;
        private final int remainingUnemploymentBenefitTurns;
        private final Integer salaryBeforeResignation;

        @Builder(access = AccessLevel.PRIVATE)
        private CareerSeedResponse(
            final JobType jobType,
            final String jobTitle,
            final int annualSalary,
            final int monthlySalary,
            final int tenureTurns,
            final int recentStudyCount,
            final int recentNetworkingCount,
            final int negotiationPreparationScore,
            final int lastNegotiatedTurn,
            final EmploymentStatus employmentStatus,
            final Integer probationEndTurn,
            final Integer rehireAvailableTurn,
            final int remainingUnemploymentBenefitTurns,
            final Integer salaryBeforeResignation
        ) {
            validateRequest(
                jobType,
                jobTitle,
                annualSalary,
                monthlySalary,
                tenureTurns,
                recentStudyCount,
                recentNetworkingCount,
                negotiationPreparationScore,
                lastNegotiatedTurn,
                employmentStatus,
                probationEndTurn,
                rehireAvailableTurn,
                remainingUnemploymentBenefitTurns,
                salaryBeforeResignation
            );

            this.jobType = jobType;
            this.jobTitle = jobTitle;
            this.annualSalary = annualSalary;
            this.monthlySalary = monthlySalary;
            this.tenureTurns = tenureTurns;
            this.recentStudyCount = recentStudyCount;
            this.recentNetworkingCount = recentNetworkingCount;
            this.negotiationPreparationScore = negotiationPreparationScore;
            this.lastNegotiatedTurn = lastNegotiatedTurn;
            this.employmentStatus = employmentStatus;
            this.probationEndTurn = probationEndTurn;
            this.rehireAvailableTurn = rehireAvailableTurn;
            this.remainingUnemploymentBenefitTurns = remainingUnemploymentBenefitTurns;
            this.salaryBeforeResignation = salaryBeforeResignation;
        }

        public static CareerSeedResponse from(final CharacterSeedPolicy.CareerSeed careerSeed) {
            return CareerSeedResponse.builder()
                .jobType(careerSeed.jobType())
                .jobTitle(careerSeed.jobTitle())
                .annualSalary(careerSeed.annualSalary())
                .monthlySalary(careerSeed.monthlySalary())
                .tenureTurns(careerSeed.tenureTurns())
                .recentStudyCount(careerSeed.recentStudyCount())
                .recentNetworkingCount(careerSeed.recentNetworkingCount())
                .negotiationPreparationScore(careerSeed.negotiationPreparationScore())
                .lastNegotiatedTurn(careerSeed.lastNegotiatedTurn())
                .employmentStatus(careerSeed.employmentStatus())
                .probationEndTurn(careerSeed.probationEndTurn())
                .rehireAvailableTurn(careerSeed.rehireAvailableTurn())
                .remainingUnemploymentBenefitTurns(
                    careerSeed.remainingUnemploymentBenefitTurns()
                )
                .salaryBeforeResignation(careerSeed.salaryBeforeResignation())
                .build();
        }

        private void validateRequest(
            final JobType jobType,
            final String jobTitle,
            final int annualSalary,
            final int monthlySalary,
            final int tenureTurns,
            final int recentStudyCount,
            final int recentNetworkingCount,
            final int negotiationPreparationScore,
            final int lastNegotiatedTurn,
            final EmploymentStatus employmentStatus,
            final Integer probationEndTurn,
            final Integer rehireAvailableTurn,
            final int remainingUnemploymentBenefitTurns,
            final Integer salaryBeforeResignation
        ) {
            if (jobType == null) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }
            if (jobTitle == null || jobTitle.isBlank()) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }
            validateNonNegative(annualSalary);
            validateNonNegative(monthlySalary);
            validateNonNegative(tenureTurns);
            validateNonNegative(recentStudyCount);
            validateNonNegative(recentNetworkingCount);
            validateNonNegative(negotiationPreparationScore);
            validateNonNegative(lastNegotiatedTurn);
            if (employmentStatus == null) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }
            validateNullableNonNegative(probationEndTurn);
            validateNullableNonNegative(rehireAvailableTurn);
            validateNonNegative(remainingUnemploymentBenefitTurns);
            validateNullableNonNegative(salaryBeforeResignation);
        }
    }

    private static void validateRange(final int value) {
        if (value < 0 || value > 100) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
    }

    private static void validateNonNegative(final int value) {
        if (value < 0) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
    }

    private static void validateNullableNonNegative(final Integer value) {
        if (value == null) {
            return;
        }

        validateNonNegative(value);
    }
}
