package io.ssafy.p.j14c103.homerun.api.service.world.response;

import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CycleJobImpact;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CycleType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CareerCycleImpactProviderResponse {

    private final CycleType cycleType;
    private final JobType jobType;
    private final JobImpactItem jobImpact;

    @Builder
    private CareerCycleImpactProviderResponse(
        final CycleType cycleType,
        final JobType jobType,
        final JobImpactItem jobImpact
    ) {
        validateCycleType(cycleType);
        validateJobType(jobType);
        validateJobImpact(jobImpact);
        this.cycleType = cycleType;
        this.jobType = jobType;
        this.jobImpact = jobImpact;
    }

    public static CareerCycleImpactProviderResponse of(
        final CycleType cycleType,
        final JobType jobType,
        final JobImpactItem jobImpact
    ) {
        return CareerCycleImpactProviderResponse.builder()
            .cycleType(cycleType)
            .jobType(jobType)
            .jobImpact(jobImpact)
            .build();
    }

    public static CareerCycleImpactProviderResponse from(
        final CycleType cycleType,
        final JobType jobType,
        final CycleJobImpact cycleJobImpact
    ) {
        return of(
            cycleType,
            jobType,
            JobImpactItem.from(cycleJobImpact)
        );
    }

    @Getter
    public static class JobImpactItem {

        private final BigDecimal salaryMultiplier;
        private final BigDecimal layoffMultiplier;
        private final int rehirePenaltyTurns;

        @Builder
        private JobImpactItem(
            final BigDecimal salaryMultiplier,
            final BigDecimal layoffMultiplier,
            final int rehirePenaltyTurns
        ) {
            validateSalaryMultiplier(salaryMultiplier);
            validateLayoffMultiplier(layoffMultiplier);
            this.salaryMultiplier = salaryMultiplier;
            this.layoffMultiplier = layoffMultiplier;
            this.rehirePenaltyTurns = rehirePenaltyTurns;
        }

        public static JobImpactItem of(
            final BigDecimal salaryMultiplier,
            final BigDecimal layoffMultiplier,
            final int rehirePenaltyTurns
        ) {
            return JobImpactItem.builder()
                .salaryMultiplier(salaryMultiplier)
                .layoffMultiplier(layoffMultiplier)
                .rehirePenaltyTurns(rehirePenaltyTurns)
                .build();
        }

        public static JobImpactItem from(final CycleJobImpact cycleJobImpact) {
            validateCycleJobImpact(cycleJobImpact);
            return of(
                cycleJobImpact.getSalaryMultiplier(),
                cycleJobImpact.getLayoffMultiplier(),
                cycleJobImpact.getRehirePenaltyTurns()
            );
        }

        private static void validateCycleJobImpact(final CycleJobImpact cycleJobImpact) {
            if (cycleJobImpact == null) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
        }

        private void validateSalaryMultiplier(final BigDecimal salaryMultiplier) {
            if (salaryMultiplier == null || salaryMultiplier.compareTo(BigDecimal.ZERO) <= 0) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
        }

        private void validateLayoffMultiplier(final BigDecimal layoffMultiplier) {
            if (layoffMultiplier == null) {
                return;
            }
            if (layoffMultiplier.compareTo(BigDecimal.ZERO) <= 0) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
        }
    }

    private static void validateCycleType(final CycleType cycleType) {
        if (cycleType == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }

    private static void validateJobType(final JobType jobType) {
        if (jobType == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }

    private static void validateJobImpact(final JobImpactItem jobImpact) {
        if (jobImpact == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }
}
