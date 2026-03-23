package io.ssafy.p.j14c103.homerun.domain.character.career;

import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class UnemploymentBenefitPolicy {

    private static final int MONTHS_PER_YEAR = 12;
    private static final int BENEFIT_RATE = 50;
    private static final int PERCENTAGE_BASE = 100;

    public UnemploymentBenefitResult calculate(final GameCareer gameCareer) {
        validateGameCareer(gameCareer);

        final EmploymentStatus employmentStatus = requireEmploymentStatus(
            gameCareer.getEmploymentStatus()
        );
        final int remainingTurns = requireRemainingTurns(
            gameCareer.getRemainingUnemploymentBenefitTurns()
        );
        if (employmentStatus != EmploymentStatus.UNEMPLOYED || remainingTurns == 0) {
            return UnemploymentBenefitResult.none();
        }

        final int salaryBeforeResignation = requirePositiveSalary(
            gameCareer.getSalaryBeforeResignation()
        );

        return UnemploymentBenefitResult.of(
            calculateBenefitAmount(salaryBeforeResignation),
            true
        );
    }

    private void validateGameCareer(final GameCareer gameCareer) {
        if (gameCareer == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
    }

    private EmploymentStatus requireEmploymentStatus(final EmploymentStatus employmentStatus) {
        if (employmentStatus == null) {
            throw new HomerunException(ErrorCode.CHARACTER_STATE_UNINITIALIZED);
        }

        return employmentStatus;
    }

    private int requireRemainingTurns(final Integer remainingTurns) {
        if (remainingTurns == null) {
            throw new HomerunException(ErrorCode.CHARACTER_STATE_UNINITIALIZED);
        }
        if (remainingTurns < 0) {
            throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
        }

        return remainingTurns;
    }

    private int requirePositiveSalary(final Integer salaryBeforeResignation) {
        if (salaryBeforeResignation == null) {
            throw new HomerunException(ErrorCode.CHARACTER_STATE_UNINITIALIZED);
        }
        if (salaryBeforeResignation <= 0) {
            throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
        }

        return salaryBeforeResignation;
    }

    private int calculateBenefitAmount(final int salaryBeforeResignation) {
        return BigDecimal.valueOf(salaryBeforeResignation)
            .divide(BigDecimal.valueOf(MONTHS_PER_YEAR), 0, RoundingMode.DOWN)
            .multiply(BigDecimal.valueOf(BENEFIT_RATE))
            .divide(BigDecimal.valueOf(PERCENTAGE_BASE), 0, RoundingMode.DOWN)
            .intValueExact();
    }

    public record UnemploymentBenefitResult(
        int benefitAmount,
        boolean benefitGranted
    ) {

        public UnemploymentBenefitResult {
            if (benefitAmount < 0) {
                throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
            }
            if (benefitGranted && benefitAmount == 0) {
                throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
            }
        }

        public static UnemploymentBenefitResult of(
            final int benefitAmount,
            final boolean benefitGranted
        ) {
            return new UnemploymentBenefitResult(benefitAmount, benefitGranted);
        }

        public static UnemploymentBenefitResult none() {
            return new UnemploymentBenefitResult(0, false);
        }
    }
}
