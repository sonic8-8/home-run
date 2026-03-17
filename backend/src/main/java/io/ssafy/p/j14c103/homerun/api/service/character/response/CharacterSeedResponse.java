package io.ssafy.p.j14c103.homerun.api.service.character.response;

import io.ssafy.p.j14c103.homerun.domain.character.CharacterSeedPolicy;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;

public record CharacterSeedResponse(
    CharacterType characterType,
    SessionSeedResponse session,
    StatSeedResponse stat,
    CareerSeedResponse career
) {

    public CharacterSeedResponse {
        if (characterType == null) {
            throw new IllegalArgumentException("characterType은 null일 수 없습니다.");
        }
        if (session == null) {
            throw new IllegalArgumentException("session은 null일 수 없습니다.");
        }
        if (stat == null) {
            throw new IllegalArgumentException("stat은 null일 수 없습니다.");
        }
        if (career == null) {
            throw new IllegalArgumentException("career는 null일 수 없습니다.");
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
                throw new IllegalArgumentException("jobTypeSummary는 null일 수 없습니다.");
            }
            if (seedType == null || seedType.isBlank()) {
                throw new IllegalArgumentException("seedType은 비어 있을 수 없습니다.");
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
                throw new IllegalArgumentException("jobType은 null일 수 없습니다.");
            }
            if (jobTitle == null || jobTitle.isBlank()) {
                throw new IllegalArgumentException("jobTitle은 비어 있을 수 없습니다.");
            }
            validateNonNegative("annualSalary", annualSalary);
            validateNonNegative("monthlySalary", monthlySalary);
            validateNonNegative("tenureTurns", tenureTurns);
            validateNonNegative("recentStudyCount", recentStudyCount);
            validateNonNegative("recentNetworkingCount", recentNetworkingCount);
            validateNonNegative("negotiationPreparationScore", negotiationPreparationScore);
            validateNonNegative("lastNegotiatedTurn", lastNegotiatedTurn);
            if (employmentStatus == null) {
                throw new IllegalArgumentException("employmentStatus는 null일 수 없습니다.");
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
            throw new IllegalArgumentException(fieldName + "는 0에서 100 사이여야 합니다.");
        }
    }

    private static void validateNonNegative(final String fieldName, final int value) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + "는 0 이상이어야 합니다.");
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
}
