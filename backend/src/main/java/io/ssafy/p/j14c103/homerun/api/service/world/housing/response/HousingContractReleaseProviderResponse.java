package io.ssafy.p.j14c103.homerun.api.service.world.housing.response;

import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.Getter;

@Getter
public class HousingContractReleaseProviderResponse {

    private final HousingType previousHousingType;
    private final HousingType nextHousingType;
    private final Money refundedDeposit;
    private final Long previousPropertyId;

    private HousingContractReleaseProviderResponse(
        final HousingType previousHousingType,
        final HousingType nextHousingType,
        final Money refundedDeposit,
        final Long previousPropertyId
    ) {
        validateHousingType(previousHousingType);
        validateHousingType(nextHousingType);
        validateRefundedDeposit(refundedDeposit);
        this.previousHousingType = previousHousingType;
        this.nextHousingType = nextHousingType;
        this.refundedDeposit = refundedDeposit;
        this.previousPropertyId = previousPropertyId;
    }

    public static HousingContractReleaseProviderResponse of(
        final HousingType previousHousingType,
        final HousingType nextHousingType,
        final Money refundedDeposit,
        final Long previousPropertyId
    ) {
        return new HousingContractReleaseProviderResponse(
            previousHousingType,
            nextHousingType,
            refundedDeposit,
            previousPropertyId
        );
    }

    private static void validateHousingType(final HousingType housingType) {
        if (housingType == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }

    private static void validateRefundedDeposit(final Money refundedDeposit) {
        if (refundedDeposit == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }
}
