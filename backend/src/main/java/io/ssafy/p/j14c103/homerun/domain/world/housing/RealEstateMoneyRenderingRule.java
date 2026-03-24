package io.ssafy.p.j14c103.homerun.domain.world.housing;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RealEstateMoneyRenderingRule {

    private String source;
    private Integer ratioPercent;
    private String roundingUnit;

    private RealEstateMoneyRenderingRule(
        String source,
        Integer ratioPercent,
        String roundingUnit
    ) {
        this.source = source;
        this.ratioPercent = ratioPercent;
        this.roundingUnit = roundingUnit;
    }

    public static RealEstateMoneyRenderingRule create(
        String source,
        Integer ratioPercent,
        String roundingUnit
    ) {
        return new RealEstateMoneyRenderingRule(
            source,
            ratioPercent,
            roundingUnit
        );
    }
}
