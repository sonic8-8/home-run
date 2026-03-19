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
            throwResponseInvalid();
        }
        if (session == null) {
            throwResponseInvalid();
        }
        if (stat == null) {
            throwResponseInvalid();
        }
        if (career == null) {
            throwResponseInvalid();
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
                throwResponseInvalid();
            }
            if (seedType == null || seedType.isBlank()) {
                throwResponseInvalid();
            }
            validateNonNegative("initialCash", initialCash);
            validateNonNegative("initialNetAssets", initialNetAssets);
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
            validateRange("health", health);
            validateRange("fatigue", fatigue);
            validateRange("stress", stress);
            validateRange("knowledge", knowledge);
            validateRange("happiness", happiness);
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
                throwResponseInvalid();
            }
            if (jobTitle == null || jobTitle.isBlank()) {
                throwResponseInvalid();
            }
            validateNonNegative("annualSalary", annualSalary);
            validateNonNegative("monthlySalary", monthlySalary);
            validateNonNegative("tenureTurns", tenureTurns);
            validateNonNegative("recentStudyCount", recentStudyCount);
            validateNonNegative("recentNetworkingCount", recentNetworkingCount);
            validateNonNegative("negotiationPreparationScore", negotiationPreparationScore);
            validateNonNegative("lastNegotiatedTurn", lastNegotiatedTurn);
            if (employmentStatus == null) {
                throwResponseInvalid();
            }
            validateNullableNonNegative("probationEndTurn", probationEndTurn);
            validateNullableNonNegative("rehireAvailableTurn", rehireAvailableTurn);
            validateNonNegative(
                "remainingUnemploymentBenefitTurns",
                remainingUnemploymentBenefitTurns
            );
            validateNullableNonNegative("salaryBeforeResignation", salaryBeforeResignation);
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

    private static void validateRange(final String fieldName, final int value) {
        if (value < 0 || value > 100) {
            throwResponseInvalid();
        }
    }

    private static void validateNonNegative(final String fieldName, final int value) {
        if (value < 0) {
            throwResponseInvalid();
        }
    }

    private static void validateNullableNonNegative(
        final String fieldName,
        final Integer value
    ) {
        if (value == null) {
            return;
        }

        validateNonNegative(fieldName, value);
    }

    private static void throwResponseInvalid() {
        throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
    }
}
