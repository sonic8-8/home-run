package io.ssafy.p.j14c103.homerun.domain.world.housing;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RealEstateRegistryRow {

    private String rankNo;
    private String purpose;
    private String receipt;
    private String reason;
    private String details;
    private Map<String, RealEstateMoneyRenderingRule> rendering;

    private RealEstateRegistryRow(
        String rankNo,
        String purpose,
        String receipt,
        String reason,
        String details,
        Map<String, RealEstateMoneyRenderingRule> rendering
    ) {
        this.rankNo = rankNo;
        this.purpose = purpose;
        this.receipt = receipt;
        this.reason = reason;
        this.details = details;
        this.rendering = rendering;
    }

    public static RealEstateRegistryRow create(
        String rankNo,
        String purpose,
        String receipt,
        String reason,
        String details,
        Map<String, RealEstateMoneyRenderingRule> rendering
    ) {
        return new RealEstateRegistryRow(
            rankNo,
            purpose,
            receipt,
            reason,
            details,
            rendering
        );
    }
}
