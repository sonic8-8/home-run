package io.ssafy.p.j14c103.homerun.api.service.world.response;

import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.Builder;
import lombok.Getter;

@Getter
public class TargetPropertyValidationResponse {

    private final Long propertyId;
    private final long priceSnapshot;
    private final HousingType housingType;

    @Builder
    private TargetPropertyValidationResponse(
        final Long propertyId,
        final long priceSnapshot,
        final HousingType housingType
    ) {
        validatePropertyId(propertyId);
        validatePriceSnapshot(priceSnapshot);
        validateHousingType(housingType);
        this.propertyId = propertyId;
        this.priceSnapshot = priceSnapshot;
        this.housingType = housingType;
    }

    public static TargetPropertyValidationResponse of(
        final Long propertyId,
        final long priceSnapshot,
        final HousingType housingType
    ) {
        return TargetPropertyValidationResponse.builder()
            .propertyId(propertyId)
            .priceSnapshot(priceSnapshot)
            .housingType(housingType)
            .build();
    }

    private static void validatePropertyId(final Long propertyId) {
        if (propertyId == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }

    private static void validatePriceSnapshot(final long priceSnapshot) {
        if (priceSnapshot < 0) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }

    private static void validateHousingType(final HousingType housingType) {
        if (housingType == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }
}
