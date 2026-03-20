package io.ssafy.p.j14c103.homerun.api.service.character.response;

import io.ssafy.p.j14c103.homerun.domain.character.CharacterSeedPolicy;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;

public record CharacterSeedResponse(
    CharacterType characterType,
    SessionSeedResponse session,
    StatSeedResponse stat,
    CareerSeedResponse career
) {

    public CharacterSeedResponse {
        if (characterType == null) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
        if (session == null) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
        if (stat == null) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
        if (career == null) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
    }

    public static CharacterSeedResponse from(final CharacterSeedPolicy.CharacterSeedPlan plan) {
        return new CharacterSeedResponse(
            plan.characterType(),
            SessionSeedResponse.from(plan.session()),
            StatSeedResponse.from(plan.stat()),
            CareerSeedResponse.from(plan.career())
        );
    }

    public record SessionSeedResponse(
        JobType jobTypeSummary,
        String seedType,
        int initialCash,
        int initialNetAssets
    ) {

        public SessionSeedResponse {
            if (jobTypeSummary == null) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }
            if (seedType == null || seedType.isBlank()) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }
            validateNonNegative(initialCash);
            validateNonNegative(initialNetAssets);
        }

        public static SessionSeedResponse from(final CharacterSeedPolicy.SessionSeed sessionSeed) {
            return new SessionSeedResponse(
                sessionSeed.jobTypeSummary(),
                sessionSeed.seedType().name(),
                sessionSeed.initialCash(),
                sessionSeed.initialNetAssets()
            );
        }
    }

    public record StatSeedResponse(
        int health,
        int fatigue,
        int stress,
        int knowledge,
        int happiness
    ) {

        public StatSeedResponse {
            validateRange(health);
            validateRange(fatigue);
            validateRange(stress);
            validateRange(knowledge);
            validateRange(happiness);
        }

        public static StatSeedResponse from(final CharacterSeedPolicy.StatSeed statSeed) {
            return new StatSeedResponse(
                statSeed.health(),
                statSeed.fatigue(),
                statSeed.stress(),
                statSeed.knowledge(),
                statSeed.happiness()
            );
        }
    }

    public record CareerSeedResponse(
        JobType jobType,
        String jobTitle,
        int annualSalary,
        int monthlySalary,
        int tenureTurns,
        int recentStudyCount,
        int recentNetworkingCount,
        int negotiationPreparationScore,
        int lastNegotiatedTurn,
        EmploymentStatus employmentStatus,
        Integer probationEndTurn,
        Integer rehireAvailableTurn,
        int remainingUnemploymentBenefitTurns,
        Integer salaryBeforeResignation
    ) {

        public CareerSeedResponse {
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

        public static CareerSeedResponse from(final CharacterSeedPolicy.CareerSeed careerSeed) {
            return new CareerSeedResponse(
                careerSeed.jobType(),
                careerSeed.jobTitle(),
                careerSeed.annualSalary(),
                careerSeed.monthlySalary(),
                careerSeed.tenureTurns(),
                careerSeed.recentStudyCount(),
                careerSeed.recentNetworkingCount(),
                careerSeed.negotiationPreparationScore(),
                careerSeed.lastNegotiatedTurn(),
                careerSeed.employmentStatus(),
                careerSeed.probationEndTurn(),
                careerSeed.rehireAvailableTurn(),
                careerSeed.remainingUnemploymentBenefitTurns(),
                careerSeed.salaryBeforeResignation()
            );
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
