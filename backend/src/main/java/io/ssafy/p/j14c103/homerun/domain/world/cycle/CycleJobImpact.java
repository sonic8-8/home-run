package io.ssafy.p.j14c103.homerun.domain.world.cycle;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import lombok.Getter;

@Getter
public class CycleJobImpact {

    private final BigDecimal salaryMultiplier;
    private final BigDecimal layoffMultiplier;
    private final int rehirePenaltyTurns;

    private CycleJobImpact(
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

    public static CycleJobImpact of(
        final BigDecimal salaryMultiplier,
        final BigDecimal layoffMultiplier,
        final int rehirePenaltyTurns
    ) {
        return new CycleJobImpact(
            salaryMultiplier,
            layoffMultiplier,
            rehirePenaltyTurns
        );
    }

    private void validateSalaryMultiplier(final BigDecimal salaryMultiplier) {
        if (salaryMultiplier == null || salaryMultiplier.compareTo(BigDecimal.ZERO) <= 0) {
            throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
        }
    }

    private void validateLayoffMultiplier(final BigDecimal layoffMultiplier) {
        if (layoffMultiplier == null) {
            return;
        }
        if (layoffMultiplier.compareTo(BigDecimal.ZERO) <= 0) {
            throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
        }
    }
}
