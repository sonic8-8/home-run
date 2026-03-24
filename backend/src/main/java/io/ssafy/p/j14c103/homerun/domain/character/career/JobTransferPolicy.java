package io.ssafy.p.j14c103.homerun.domain.character.career;

import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class JobTransferPolicy {

    private static final int MIN_KNOWLEDGE = 0;
    private static final int MAX_KNOWLEDGE = 100;
    private static final int LOW_KNOWLEDGE_MAX = 30;
    private static final int MID_KNOWLEDGE_MAX = 60;
    private static final int MEET_FRIEND_BONUS_THRESHOLD = 2;
    private static final int OFFER_CHANCE_BONUS_RATE = 10;
    private static final int MIN_MULTIPLIER_BASIS_POINTS = 9_000;
    private static final int MULTIPLIER_SPAN_BASIS_POINTS = 6_000;
    private static final int BASIS_POINTS_DIVISOR = 10_000;
    private static final int STANDARD_PROBATION_TURNS = 1;
    private static final int EXTENDED_PROBATION_TURNS = 2;

    private static final List<JobType> JOB_TYPE_ORDER = List.of(
        JobType.SMALL_BIZ,
        JobType.MID_BIZ,
        JobType.STARTUP,
        JobType.LARGE_BIZ,
        JobType.FREELANCER
    );

    private static final Map<JobType, Integer> CORPORATE_LEVELS = Map.of(
        JobType.SMALL_BIZ, 0,
        JobType.MID_BIZ, 1,
        JobType.STARTUP, 1,
        JobType.LARGE_BIZ, 2
    );

    private static final Map<JobType, Integer> MARKET_BASE_SALARIES = Map.of(
        JobType.SMALL_BIZ, 30_000_000,
        JobType.MID_BIZ, 32_000_000,
        JobType.STARTUP, 31_000_000,
        JobType.LARGE_BIZ, 42_000_000,
        JobType.FREELANCER, 28_000_000
    );

    private static final Map<JobType, String> DISPLAY_COMPANY_NAMES = Map.of(
        JobType.SMALL_BIZ, "OO 중소기업",
        JobType.MID_BIZ, "OO 중견기업",
        JobType.STARTUP, "OO 스타트업",
        JobType.LARGE_BIZ, "OO 대기업",
        JobType.FREELANCER, "OO 프리랜서 프로젝트"
    );

    public JobOfferPool calculateOfferPool(
        final GameCareer gameCareer,
        final GameStat gameStat,
        final int recentMeetFriendCount
    ) {
        validateGameCareer(gameCareer);
        validateGameStat(gameStat);
        validateRecentMeetFriendCount(recentMeetFriendCount);

        final EmploymentStatus employmentStatus = requireEmploymentStatus(
            gameCareer.getEmploymentStatus()
        );
        if (employmentStatus == EmploymentStatus.UNEMPLOYED) {
            return JobOfferPool.empty();
        }

        final JobType currentJobType = requireJobType(gameCareer.getJobType());
        final int currentSalary = requirePositiveSalary(gameCareer.getSalary());
        final int knowledge = requireKnowledge(gameStat.getKnowledge());
        final List<JobType> offerJobTypes = resolveOfferJobTypes(currentJobType, knowledge);
        final int offerChanceBonusRate = resolveOfferChanceBonusRate(recentMeetFriendCount);

        return JobOfferPool.of(
            createOffers(currentJobType, currentSalary, knowledge, offerJobTypes),
            offerChanceBonusRate,
            offerChanceBonusRate > 0
        );
    }

    public JobOffer resolveOffer(
        final GameCareer gameCareer,
        final GameStat gameStat,
        final int recentMeetFriendCount,
        final String offerId
    ) {
        validateOfferId(offerId);

        return calculateOfferPool(gameCareer, gameStat, recentMeetFriendCount).offers().stream()
            .filter(offer -> offer.offerId().equals(offerId))
            .findFirst()
            .orElseThrow(() -> new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID));
    }

    private void validateGameCareer(final GameCareer gameCareer) {
        if (gameCareer == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
    }

    private void validateGameStat(final GameStat gameStat) {
        if (gameStat == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
    }

    private void validateRecentMeetFriendCount(final int recentMeetFriendCount) {
        if (recentMeetFriendCount < 0) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
    }

    private void validateOfferId(final String offerId) {
        if (offerId == null || offerId.isBlank()) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
    }

    private EmploymentStatus requireEmploymentStatus(final EmploymentStatus employmentStatus) {
        if (employmentStatus == null) {
            throw new HomerunException(ErrorCode.CHARACTER_STATE_UNINITIALIZED);
        }

        return employmentStatus;
    }

    private JobType requireJobType(final JobType jobType) {
        if (jobType == null) {
            throw new HomerunException(ErrorCode.CHARACTER_STATE_UNINITIALIZED);
        }

        return jobType;
    }

    private int requirePositiveSalary(final Integer salary) {
        if (salary == null) {
            throw new HomerunException(ErrorCode.CHARACTER_STATE_UNINITIALIZED);
        }
        if (salary <= 0) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }

        return salary;
    }

    private int requireKnowledge(final Integer knowledge) {
        if (knowledge == null) {
            throw new HomerunException(ErrorCode.CHARACTER_STATE_UNINITIALIZED);
        }
        if (knowledge < MIN_KNOWLEDGE || knowledge > MAX_KNOWLEDGE) {
            throw new HomerunException(ErrorCode.CHARACTER_STAT_INVALID);
        }

        return knowledge;
    }

    private List<JobType> resolveOfferJobTypes(final JobType currentJobType, final int knowledge) {
        if (currentJobType == JobType.FREELANCER) {
            return resolveFreelancerOfferJobTypes(knowledge);
        }

        return resolveCorporateOfferJobTypes(currentJobType, knowledge);
    }

    private List<JobType> resolveCorporateOfferJobTypes(
        final JobType currentJobType,
        final int knowledge
    ) {
        final int currentLevel = resolveCorporateLevel(currentJobType);

        if (knowledge <= LOW_KNOWLEDGE_MAX) {
            final EnumSet<JobType> availableJobTypes = EnumSet.of(currentJobType);
            addLevelJobTypes(availableJobTypes, currentLevel - 1);
            availableJobTypes.add(JobType.FREELANCER);
            return sortJobTypes(currentJobType, availableJobTypes);
        }

        final EnumSet<JobType> availableJobTypes = EnumSet.noneOf(JobType.class);
        addLevelJobTypes(availableJobTypes, currentLevel);
        addLevelJobTypes(availableJobTypes, currentLevel + 1);
        if (knowledge > MID_KNOWLEDGE_MAX) {
            addLevelJobTypes(availableJobTypes, currentLevel + 2);
        }
        availableJobTypes.add(JobType.FREELANCER);

        return sortJobTypes(currentJobType, availableJobTypes);
    }

    private List<JobType> resolveFreelancerOfferJobTypes(final int knowledge) {
        if (knowledge <= LOW_KNOWLEDGE_MAX) {
            return List.of(JobType.FREELANCER, JobType.SMALL_BIZ);
        }
        if (knowledge <= MID_KNOWLEDGE_MAX) {
            return List.of(
                JobType.FREELANCER,
                JobType.SMALL_BIZ,
                JobType.MID_BIZ,
                JobType.STARTUP
            );
        }

        return List.of(
            JobType.FREELANCER,
            JobType.SMALL_BIZ,
            JobType.MID_BIZ,
            JobType.STARTUP,
            JobType.LARGE_BIZ
        );
    }

    private int resolveCorporateLevel(final JobType jobType) {
        final Integer corporateLevel = CORPORATE_LEVELS.get(jobType);
        if (corporateLevel == null) {
            throw new HomerunException(ErrorCode.CHARACTER_JOB_TYPE_UNSUPPORTED);
        }

        return corporateLevel;
    }

    private void addLevelJobTypes(
        final EnumSet<JobType> availableJobTypes,
        final int corporateLevel
    ) {
        CORPORATE_LEVELS.entrySet().stream()
            .filter(entry -> entry.getValue() == corporateLevel)
            .map(Map.Entry::getKey)
            .forEach(availableJobTypes::add);
    }

    private List<JobType> sortJobTypes(
        final JobType currentJobType,
        final EnumSet<JobType> availableJobTypes
    ) {
        final List<JobType> sortedJobTypes = new ArrayList<>();
        if (availableJobTypes.contains(currentJobType)) {
            sortedJobTypes.add(currentJobType);
        }

        JOB_TYPE_ORDER.stream()
            .filter(availableJobTypes::contains)
            .filter(jobType -> jobType != currentJobType)
            .forEach(sortedJobTypes::add);

        return List.copyOf(sortedJobTypes);
    }

    private int resolveOfferChanceBonusRate(final int recentMeetFriendCount) {
        if (recentMeetFriendCount < MEET_FRIEND_BONUS_THRESHOLD) {
            return 0;
        }

        return OFFER_CHANCE_BONUS_RATE;
    }

    private List<JobOffer> createOffers(
        final JobType currentJobType,
        final int currentSalary,
        final int knowledge,
        final List<JobType> offerJobTypes
    ) {
        final List<JobOffer> offers = new ArrayList<>();

        for (int index = 0; index < offerJobTypes.size(); index++) {
            final JobType offerJobType = offerJobTypes.get(index);
            offers.add(JobOffer.of(
                "OFFER-%03d".formatted(index + 1),
                offerJobType,
                DISPLAY_COMPANY_NAMES.get(offerJobType),
                currentSalary,
                calculateOfferedSalary(currentSalary, knowledge, offerJobType),
                resolveProbationTurns(currentJobType, offerJobType)
            ));
        }

        return List.copyOf(offers);
    }

    private int calculateOfferedSalary(
        final int currentSalary,
        final int knowledge,
        final JobType offerJobType
    ) {
        final BigDecimal multiplier = BigDecimal.valueOf(resolveMultiplierBasisPoints(knowledge));
        final int calculatedSalary = BigDecimal.valueOf(currentSalary)
            .multiply(multiplier)
            .divide(BigDecimal.valueOf(BASIS_POINTS_DIVISOR), 0, RoundingMode.DOWN)
            .intValue();

        return Math.max(calculatedSalary, MARKET_BASE_SALARIES.get(offerJobType));
    }

    private int resolveMultiplierBasisPoints(final int knowledge) {
        return MIN_MULTIPLIER_BASIS_POINTS
            + ((knowledge * MULTIPLIER_SPAN_BASIS_POINTS) / MAX_KNOWLEDGE);
    }

    private Integer resolveProbationTurns(
        final JobType currentJobType,
        final JobType offerJobType
    ) {
        if (offerJobType == JobType.FREELANCER) {
            return null;
        }
        if (currentJobType == JobType.FREELANCER) {
            return EXTENDED_PROBATION_TURNS;
        }
        if (resolveCorporateLevel(offerJobType) > resolveCorporateLevel(currentJobType)) {
            return EXTENDED_PROBATION_TURNS;
        }

        return STANDARD_PROBATION_TURNS;
    }

    public record JobOfferPool(
        List<JobOffer> offers,
        int offerChanceBonusRate,
        boolean meetFriendBonusApplied
    ) {

        public JobOfferPool {
            if (offers == null) {
                throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
            }
            if (offers.stream().anyMatch(java.util.Objects::isNull)) {
                throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
            }
            if (offerChanceBonusRate < 0) {
                throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
            }
            if ((offerChanceBonusRate > 0) != meetFriendBonusApplied) {
                throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
            }
            offers = List.copyOf(offers);
        }

        public static JobOfferPool of(
            final List<JobOffer> offers,
            final int offerChanceBonusRate,
            final boolean meetFriendBonusApplied
        ) {
            return new JobOfferPool(offers, offerChanceBonusRate, meetFriendBonusApplied);
        }

        public static JobOfferPool empty() {
            return new JobOfferPool(List.of(), 0, false);
        }
    }

    public record JobOffer(
        String offerId,
        JobType jobType,
        String displayCompanyName,
        int currentSalary,
        int offeredSalary,
        Integer probationTurns
    ) {

        public JobOffer {
            if (offerId == null || offerId.isBlank()) {
                throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
            }
            if (jobType == null) {
                throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
            }
            if (displayCompanyName == null || displayCompanyName.isBlank()) {
                throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
            }
            if (currentSalary <= 0) {
                throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
            }
            if (offeredSalary <= 0) {
                throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
            }
            if (jobType == JobType.FREELANCER && probationTurns != null) {
                throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
            }
            if (jobType != JobType.FREELANCER && probationTurns == null) {
                throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
            }
            if (probationTurns != null && probationTurns <= 0) {
                throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
            }
        }

        public static JobOffer of(
            final String offerId,
            final JobType jobType,
            final String displayCompanyName,
            final int currentSalary,
            final int offeredSalary,
            final Integer probationTurns
        ) {
            return new JobOffer(
                offerId,
                jobType,
                displayCompanyName,
                currentSalary,
                offeredSalary,
                probationTurns
            );
        }
    }
}
