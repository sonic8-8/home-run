package io.ssafy.p.j14c103.homerun.domain.world.housing;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContractTrap {
    /*
        계약 검토 과정에서 탐지해야 하는 함정 항목
     */

    private String trapId;
    private String type;
    private String documentType;
    private String description;
    private ContractTrapPenalty penalty;

    private ContractTrap(
        String trapId,
        String type,
        String documentType,
        String description,
        ContractTrapPenalty penalty
    ) {
        this.trapId = trapId;
        this.type = type;
        this.documentType = documentType;
        this.description = description;
        this.penalty = penalty;
    }

    public static ContractTrap create(
        String trapId,
        String type,
        String documentType,
        String description,
        ContractTrapPenalty penalty
    ) {
        return new ContractTrap(
            trapId,
            type,
            documentType,
            description,
            penalty
        );
    }
}
