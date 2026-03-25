package io.ssafy.p.j14c103.homerun.domain.character.career;

import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public record CareerCycleEffect(
    BigDecimal salaryMultiplier,
    BigDecimal layoffMultiplier,
    int rehirePenaltyTurns
) {

    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal SMALL_BIZ_BOOM_SALARY_MULTIPLIER = BigDecimal.valueOf(1.3);
    private static final BigDecimal MID_BIZ_BOOM_SALARY_MULTIPLIER = BigDecimal.valueOf(1.4);
    private static final BigDecimal LARGE_BIZ_BOOM_SALARY_MULTIPLIER = BigDecimal.valueOf(1.2);
    private static final BigDecimal FREELANCER_BOOM_SALARY_MULTIPLIER = BigDecimal.valueOf(1.5);
    private static final BigDecimal SMALL_BIZ_BOOM_LAYOFF_MULTIPLIER = BigDecimal.valueOf(0.7);
    private static final BigDecimal MID_BIZ_BOOM_LAYOFF_MULTIPLIER = BigDecimal.valueOf(0.6);
    private static final BigDecimal LARGE_BIZ_BOOM_LAYOFF_MULTIPLIER = BigDecimal.valueOf(0.5);
    private static final int SMALL_BIZ_BOOM_REHIRE_PENALTY = 0;
    private static final int MID_BIZ_BOOM_REHIRE_PENALTY = 0;
    private static final int LARGE_BIZ_BOOM_REHIRE_PENALTY = 0;
    private static final int FREELANCER_BOOM_REHIRE_PENALTY = -1;

    private static final BigDecimal SMALL_BIZ_CRISIS_SALARY_MULTIPLIER = BigDecimal.valueOf(0.5);
    private static final BigDecimal MID_BIZ_CRISIS_SALARY_MULTIPLIER = BigDecimal.valueOf(0.6);
    private static final BigDecimal LARGE_BIZ_CRISIS_SALARY_MULTIPLIER = BigDecimal.valueOf(0.7);
    private static final BigDecimal FREELANCER_CRISIS_SALARY_MULTIPLIER = BigDecimal.valueOf(0.4);
    private static final BigDecimal SMALL_BIZ_CRISIS_LAYOFF_MULTIPLIER = BigDecimal.valueOf(2.5);
    private static final BigDecimal MID_BIZ_CRISIS_LAYOFF_MULTIPLIER = BigDecimal.valueOf(2.0);
    private static final BigDecimal LARGE_BIZ_CRISIS_LAYOFF_MULTIPLIER = BigDecimal.valueOf(1.5);
    private static final int SMALL_BIZ_CRISIS_REHIRE_PENALTY = 2;
    private static final int MID_BIZ_CRISIS_REHIRE_PENALTY = 2;
    private static final int LARGE_BIZ_CRISIS_REHIRE_PENALTY = 1;
    private static final int FREELANCER_CRISIS_REHIRE_PENALTY = 3;
    private static final int MIN_REHIRE_WAIT_TURNS = 1;

    public CareerCycleEffect {
        validateMultiplier(salaryMultiplier);
        validateOptionalMultiplier(layoffMultiplier);
    }

    public static CareerCycleEffect identity() {
        return new CareerCycleEffect(ONE, ONE, 0);
    }

    public static CareerCycleEffect from(
        final CyclePhase cyclePhase,
        final JobType jobType
    ) {
        final JobType currentJobType = requireJobType(jobType);
        if (cyclePhase == null || cyclePhase == CyclePhase.RECOVERY) {
            return identity();
        }
        if (cyclePhase == CyclePhase.BOOM) {
            return resolveBoomEffect(currentJobType);
        }

        return resolveCrisisEffect(currentJobType);
    }

    public int applySalaryMultiplier(final int raiseRate) {
        if (raiseRate < 0) {
            throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
        }

        return BigDecimal.valueOf(raiseRate)
            .multiply(salaryMultiplier)
            .setScale(0, RoundingMode.DOWN)
            .intValueExact();
    }

    public int applyRehirePenalty(final int baseWaitTurns) {
        if (baseWaitTurns < MIN_REHIRE_WAIT_TURNS) {
            throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
        }

        return Math.max(MIN_REHIRE_WAIT_TURNS, baseWaitTurns + rehirePenaltyTurns);
    }

    private static CareerCycleEffect resolveBoomEffect(final JobType jobType) {
        if (jobType == JobType.SMALL_BIZ) {
            return new CareerCycleEffect(
                SMALL_BIZ_BOOM_SALARY_MULTIPLIER,
                SMALL_BIZ_BOOM_LAYOFF_MULTIPLIER,
                SMALL_BIZ_BOOM_REHIRE_PENALTY
            );
        }
        if (jobType == JobType.MID_BIZ || jobType == JobType.STARTUP) {
            return new CareerCycleEffect(
                MID_BIZ_BOOM_SALARY_MULTIPLIER,
                MID_BIZ_BOOM_LAYOFF_MULTIPLIER,
                MID_BIZ_BOOM_REHIRE_PENALTY
            );
        }
        if (jobType == JobType.LARGE_BIZ) {
            return new CareerCycleEffect(
                LARGE_BIZ_BOOM_SALARY_MULTIPLIER,
                LARGE_BIZ_BOOM_LAYOFF_MULTIPLIER,
                LARGE_BIZ_BOOM_REHIRE_PENALTY
            );
        }
        if (jobType == JobType.FREELANCER) {
            return new CareerCycleEffect(
                FREELANCER_BOOM_SALARY_MULTIPLIER,
                null,
                FREELANCER_BOOM_REHIRE_PENALTY
            );
        }

        throw new HomerunException(ErrorCode.CHARACTER_JOB_TYPE_UNSUPPORTED);
    }

    private static CareerCycleEffect resolveCrisisEffect(final JobType jobType) {
        if (jobType == JobType.SMALL_BIZ) {
            return new CareerCycleEffect(
                SMALL_BIZ_CRISIS_SALARY_MULTIPLIER,
                SMALL_BIZ_CRISIS_LAYOFF_MULTIPLIER,
                SMALL_BIZ_CRISIS_REHIRE_PENALTY
            );
        }
        if (jobType == JobType.MID_BIZ || jobType == JobType.STARTUP) {
            return new CareerCycleEffect(
                MID_BIZ_CRISIS_SALARY_MULTIPLIER,
                MID_BIZ_CRISIS_LAYOFF_MULTIPLIER,
                MID_BIZ_CRISIS_REHIRE_PENALTY
            );
        }
        if (jobType == JobType.LARGE_BIZ) {
            return new CareerCycleEffect(
                LARGE_BIZ_CRISIS_SALARY_MULTIPLIER,
                LARGE_BIZ_CRISIS_LAYOFF_MULTIPLIER,
                LARGE_BIZ_CRISIS_REHIRE_PENALTY
            );
        }
        if (jobType == JobType.FREELANCER) {
            return new CareerCycleEffect(
                FREELANCER_CRISIS_SALARY_MULTIPLIER,
                null,
                FREELANCER_CRISIS_REHIRE_PENALTY
            );
        }

        throw new HomerunException(ErrorCode.CHARACTER_JOB_TYPE_UNSUPPORTED);
    }

    private static JobType requireJobType(final JobType jobType) {
        if (jobType == null) {
            throw new HomerunException(ErrorCode.CHARACTER_STATE_UNINITIALIZED);
        }

        return jobType;
    }

    private static void validateMultiplier(final BigDecimal multiplier) {
        if (multiplier == null || multiplier.compareTo(BigDecimal.ZERO) <= 0) {
            throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
        }
    }

    private static void validateOptionalMultiplier(final BigDecimal multiplier) {
        if (multiplier == null) {
            return;
        }
        if (multiplier.compareTo(BigDecimal.ZERO) <= 0) {
            throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
        }
    }
}
