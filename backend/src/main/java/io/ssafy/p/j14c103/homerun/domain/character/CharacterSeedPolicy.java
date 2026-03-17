package io.ssafy.p.j14c103.homerun.domain.character;

import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import java.util.List;

public class CharacterSeedPolicy {

    private static final int MY_DATA_INITIAL_CASH = 13_000_000;
    private static final int DEFAULT_INITIAL_CASH = 10_000_000;
    private static final String ENTRY_LEVEL_EMPLOYEE_JOB_TITLE = "수습/인턴";
    private static final String ENTRY_LEVEL_FREELANCER_JOB_TITLE = "신입";
    private static final int BASE_HEALTH = 70;
    private static final int BASE_FATIGUE = 10;
    private static final int BASE_STRESS = 10;
    private static final int BASE_KNOWLEDGE = 50;
    private static final int BASE_HAPPINESS = 50;
    private static final int DEFAULT_TENURE_TURNS = 0;
    private static final int DEFAULT_RECENT_STUDY_COUNT = 0;
    private static final int DEFAULT_RECENT_NETWORKING_COUNT = 0;
    private static final int DEFAULT_NEGOTIATION_PREPARATION_SCORE = 0;
    private static final int DEFAULT_LAST_NEGOTIATED_TURN = 0;
    private static final int DEFAULT_UNEMPLOYMENT_BENEFIT_TURNS = 0;
    private static final int DEFAULT_PROBATION_END_TURN = 6;

    private final List<JobTypeSeedProfile> jobTypeProfiles = List.of(
        new JobTypeSeedProfile(
            JobType.LARGE_BIZ,
            "대기업 직장인",
            JobTypeGauge.of(80, 60, 90, 40, 70),
            StatOffset.of(5, -5, -5, 0, 0),
            42_000_000,
            3_500_000,
            ENTRY_LEVEL_EMPLOYEE_JOB_TITLE,
            EmploymentStatus.PROBATION,
            DEFAULT_PROBATION_END_TURN
        ),
        new JobTypeSeedProfile(
            JobType.MID_BIZ,
            "중견기업 직장인",
            JobTypeGauge.of(60, 70, 70, 50, 50),
            StatOffset.of(0, 0, 0, 0, 0),
            32_000_000,
            2_666_666,
            ENTRY_LEVEL_EMPLOYEE_JOB_TITLE,
            EmploymentStatus.PROBATION,
            DEFAULT_PROBATION_END_TURN
        ),
        new JobTypeSeedProfile(
            JobType.SMALL_BIZ,
            "중소기업 직장인",
            JobTypeGauge.of(40, 80, 50, 60, 30),
            StatOffset.of(-5, 5, 5, 0, 0),
            30_000_000,
            2_500_000,
            ENTRY_LEVEL_EMPLOYEE_JOB_TITLE,
            EmploymentStatus.PROBATION,
            DEFAULT_PROBATION_END_TURN
        ),
        new JobTypeSeedProfile(
            JobType.STARTUP,
            "스타트업 직장인",
            JobTypeGauge.of(55, 55, 35, 85, 80),
            StatOffset.of(-5, 8, 8, 5, -3),
            31_000_000,
            2_583_333,
            ENTRY_LEVEL_EMPLOYEE_JOB_TITLE,
            EmploymentStatus.PROBATION,
            DEFAULT_PROBATION_END_TURN
        ),
        new JobTypeSeedProfile(
            JobType.FREELANCER,
            "프리랜서",
            JobTypeGauge.of(50, 50, 20, 90, 85),
            StatOffset.of(-2, 2, 5, 3, 5),
            28_000_000,
            2_333_333,
            ENTRY_LEVEL_FREELANCER_JOB_TITLE,
            EmploymentStatus.EMPLOYED,
            null
        )
    );

    public CharacterSeedPlan calculate(
        final CharacterType characterType,
        final JobType jobType,
        final SeedType seedType
    ) {
        validateNotNull(characterType, "characterType");
        validateNotNull(jobType, "jobType");
        validateNotNull(seedType, "seedType");

        final JobTypeSeedProfile profile = findJobTypeProfile(jobType);
        final int initialCash = resolveInitialCash(seedType);

        return new CharacterSeedPlan(
            characterType,
            SessionSeed.of(jobType, seedType, initialCash),
            StatSeed.from(profile.statOffset()),
            CareerSeed.from(profile)
        );
    }

    public List<JobTypeSeedProfile> getJobTypeProfiles() {
        return jobTypeProfiles;
    }

    private JobTypeSeedProfile findJobTypeProfile(final JobType jobType) {
        return jobTypeProfiles.stream()
            .filter(profile -> profile.jobType() == jobType)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 jobType입니다: " + jobType));
    }

    private int resolveInitialCash(final SeedType seedType) {
        if (seedType == SeedType.MY_DATA) {
            return MY_DATA_INITIAL_CASH;
        }

        return DEFAULT_INITIAL_CASH;
    }

    private void validateNotNull(final Object value, final String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + "은 null일 수 없습니다.");
        }
    }

    public record CharacterSeedPlan(
        CharacterType characterType,
        SessionSeed session,
        StatSeed stat,
        CareerSeed career
    ) {

        public CharacterSeedPlan {
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
    }

    public record SessionSeed(
        JobType jobTypeSummary,
        SeedType seedType,
        int initialCash,
        int initialNetAssets
    ) {

        public SessionSeed {
            if (jobTypeSummary == null) {
                throw new IllegalArgumentException("jobTypeSummary는 null일 수 없습니다.");
            }
            if (seedType == null) {
                throw new IllegalArgumentException("seedType은 null일 수 없습니다.");
            }
            validateNonNegative("initialCash", initialCash);
            validateNonNegative("initialNetAssets", initialNetAssets);
        }

        public static SessionSeed of(
            final JobType jobTypeSummary,
            final SeedType seedType,
            final int initialCash
        ) {
            return new SessionSeed(jobTypeSummary, seedType, initialCash, initialCash);
        }
    }

    public record StatSeed(
        int health,
        int fatigue,
        int stress,
        int knowledge,
        int happiness
    ) {

        public StatSeed {
            validateStatRange("health", health);
            validateStatRange("fatigue", fatigue);
            validateStatRange("stress", stress);
            validateStatRange("knowledge", knowledge);
            validateStatRange("happiness", happiness);
        }

        public static StatSeed from(final StatOffset statOffset) {
            return new StatSeed(
                BASE_HEALTH + statOffset.healthOffset(),
                BASE_FATIGUE + statOffset.fatigueOffset(),
                BASE_STRESS + statOffset.stressOffset(),
                BASE_KNOWLEDGE + statOffset.knowledgeOffset(),
                BASE_HAPPINESS + statOffset.happinessOffset()
            );
        }
    }

    public record CareerSeed(
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

        public CareerSeed {
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

        public static CareerSeed from(final JobTypeSeedProfile profile) {
            return new CareerSeed(
                profile.jobType(),
                profile.initialJobTitle(),
                profile.initialAnnualSalary(),
                profile.initialMonthlySalary(),
                DEFAULT_TENURE_TURNS,
                DEFAULT_RECENT_STUDY_COUNT,
                DEFAULT_RECENT_NETWORKING_COUNT,
                DEFAULT_NEGOTIATION_PREPARATION_SCORE,
                DEFAULT_LAST_NEGOTIATED_TURN,
                profile.initialEmploymentStatus(),
                profile.probationEndTurn(),
                null,
                DEFAULT_UNEMPLOYMENT_BENEFIT_TURNS,
                null
            );
        }
    }

    public record JobTypeSeedProfile(
        JobType jobType,
        String label,
        JobTypeGauge gauge,
        StatOffset statOffset,
        int initialAnnualSalary,
        int initialMonthlySalary,
        String initialJobTitle,
        EmploymentStatus initialEmploymentStatus,
        Integer probationEndTurn
    ) {

        public JobTypeSeedProfile {
            if (jobType == null) {
                throw new IllegalArgumentException("jobType은 null일 수 없습니다.");
            }
            if (label == null || label.isBlank()) {
                throw new IllegalArgumentException("label은 비어 있을 수 없습니다.");
            }
            if (gauge == null) {
                throw new IllegalArgumentException("gauge는 null일 수 없습니다.");
            }
            if (statOffset == null) {
                throw new IllegalArgumentException("statOffset은 null일 수 없습니다.");
            }
            validateNonNegative("initialAnnualSalary", initialAnnualSalary);
            validateNonNegative("initialMonthlySalary", initialMonthlySalary);
            if (initialJobTitle == null || initialJobTitle.isBlank()) {
                throw new IllegalArgumentException("initialJobTitle은 비어 있을 수 없습니다.");
            }
            if (initialEmploymentStatus == null) {
                throw new IllegalArgumentException("initialEmploymentStatus는 null일 수 없습니다.");
            }
            validateNullableNonNegative("probationEndTurn", probationEndTurn);
        }
    }

    public record JobTypeGauge(
        int salary,
        int health,
        int stability,
        int growthSpeed,
        int difficulty
    ) {

        public JobTypeGauge {
            validateStatRange("salary", salary);
            validateStatRange("health", health);
            validateStatRange("stability", stability);
            validateStatRange("growthSpeed", growthSpeed);
            validateStatRange("difficulty", difficulty);
        }

        public static JobTypeGauge of(
            final int salary,
            final int health,
            final int stability,
            final int growthSpeed,
            final int difficulty
        ) {
            return new JobTypeGauge(salary, health, stability, growthSpeed, difficulty);
        }
    }

    public record StatOffset(
        int healthOffset,
        int fatigueOffset,
        int stressOffset,
        int knowledgeOffset,
        int happinessOffset
    ) {

        public static StatOffset of(
            final int healthOffset,
            final int fatigueOffset,
            final int stressOffset,
            final int knowledgeOffset,
            final int happinessOffset
        ) {
            return new StatOffset(
                healthOffset,
                fatigueOffset,
                stressOffset,
                knowledgeOffset,
                happinessOffset
            );
        }
    }

    private static void validateStatRange(final String fieldName, final int value) {
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
