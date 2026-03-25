package io.ssafy.p.j14c103.homerun.api.service.world.housing.response;

import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import lombok.Getter;

@Getter
public class RealEstatePropertyDetailProviderResponse {

    private final Long propertyId;
    private final String name;
    private final long recentPrice;
    private final String address;
    private final BigDecimal latitude;
    private final BigDecimal longitude;
    private final HousingType housingType;

    private RealEstatePropertyDetailProviderResponse(
        final Long propertyId,
        final String name,
        final long recentPrice,
        final String address,
        final BigDecimal latitude,
        final BigDecimal longitude,
        final HousingType housingType
    ) {
        validatePropertyId(propertyId);
        validateName(name);
        validatePrice(recentPrice);
        validateAddress(address);
        validateCoordinate(latitude);
        validateCoordinate(longitude);
        validateHousingType(housingType);

        this.propertyId = propertyId;
        this.name = name;
        this.recentPrice = recentPrice;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.housingType = housingType;
    }

    public static RealEstatePropertyDetailProviderResponse of(
        final Long propertyId,
        final String name,
        final long recentPrice,
        final String address,
        final BigDecimal latitude,
        final BigDecimal longitude,
        final HousingType housingType
    ) {
        return new RealEstatePropertyDetailProviderResponse(
            propertyId,
            name,
            recentPrice,
            address,
            latitude,
            longitude,
            housingType
        );
    }

    private static void validatePropertyId(final Long propertyId) {
        if (propertyId == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }

    private static void validateName(final String name) {
        if (name == null || name.isBlank()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }

    private static void validatePrice(final long recentPrice) {
        if (recentPrice < 0) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }

    private static void validateAddress(final String address) {
        if (address == null || address.isBlank()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }

    private static void validateCoordinate(final BigDecimal coordinate) {
        if (coordinate == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }

    private static void validateHousingType(final HousingType housingType) {
        if (housingType == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }
}
