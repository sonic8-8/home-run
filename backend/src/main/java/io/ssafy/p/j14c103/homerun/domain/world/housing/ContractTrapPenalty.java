package io.ssafy.p.j14c103.homerun.domain.world.housing;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContractTrapPenalty {

    private String cash;
    private Integer stress;

    private ContractTrapPenalty(
        String cash,
        Integer stress
    ) {
        this.cash = cash;
        this.stress = stress;
    }

    public static ContractTrapPenalty create(
        String cash,
        Integer stress
    ) {
        return new ContractTrapPenalty(
            cash,
            stress
        );
    }
}
