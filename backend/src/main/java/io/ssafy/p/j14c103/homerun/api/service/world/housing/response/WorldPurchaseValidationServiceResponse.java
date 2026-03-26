package io.ssafy.p.j14c103.homerun.api.service.world.housing.response;

import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.Getter;

@Getter
public class WorldPurchaseValidationServiceResponse {

    private final boolean passed;
    private final PurchaseValidationFailureCode failureCode;
    private final Long propertyId;
    private final Long purchasePrice;
    private final HousingType housingType;

    private WorldPurchaseValidationServiceResponse(
        final boolean passed,
        final PurchaseValidationFailureCode failureCode,
        final Long propertyId,
        final Long purchasePrice,
        final HousingType housingType
    ) {
        validate(passed, failureCode, propertyId, purchasePrice, housingType);

        this.passed = passed;
        this.failureCode = failureCode;
        this.propertyId = propertyId;
        this.purchasePrice = purchasePrice;
        this.housingType = housingType;
    }

    public static WorldPurchaseValidationServiceResponse success(
        final Long propertyId,
        final Long purchasePrice,
        final HousingType housingType
    ) {
        return new WorldPurchaseValidationServiceResponse(
            true,
            null,
            propertyId,
            purchasePrice,
            housingType
        );
    }

    public static WorldPurchaseValidationServiceResponse failure(
        final PurchaseValidationFailureCode failureCode
    ) {
        return new WorldPurchaseValidationServiceResponse(
            false,
            failureCode,
            null,
            null,
            null
        );
    }

    private void validate(
        final boolean passed,
        final PurchaseValidationFailureCode failureCode,
        final Long propertyId,
        final Long purchasePrice,
        final HousingType housingType
    ) {
        if (passed) {
            if (failureCode != null || propertyId == null || purchasePrice == null || purchasePrice < 0 || housingType == null) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
            return;
        }

        if (failureCode == null || propertyId != null || purchasePrice != null || housingType != null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }
}
