package io.ssafy.p.j14c103.homerun.api.service.game.realestate.response;

import io.ssafy.p.j14c103.homerun.api.service.world.housing.response.RealEstatePropertyDetailProviderResponse;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import lombok.Getter;

@Getter
public class RealEstatePropertyDetailResponse {

    private final Long propertyId;
    private final String name;
    private final long recentPrice;
    private final String address;
    private final BigDecimal latitude;
    private final BigDecimal longitude;
    private final HousingType housingType;

    private RealEstatePropertyDetailResponse(
        final Long propertyId,
        final String name,
        final long recentPrice,
        final String address,
        final BigDecimal latitude,
        final BigDecimal longitude,
        final HousingType housingType
    ) {
        validatePropertyId(propertyId);
        validateText(name);
        validateRecentPrice(recentPrice);
        validateText(address);
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

    public static RealEstatePropertyDetailResponse of(
        final Long propertyId,
        final String name,
        final long recentPrice,
        final String address,
        final BigDecimal latitude,
        final BigDecimal longitude,
        final HousingType housingType
    ) {
        return new RealEstatePropertyDetailResponse(
            propertyId,
            name,
            recentPrice,
            address,
            latitude,
            longitude,
            housingType
        );
    }

    public static RealEstatePropertyDetailResponse from(
        final RealEstatePropertyDetailProviderResponse providerResponse
    ) {
        return of(
            providerResponse.getPropertyId(),
            providerResponse.getName(),
            providerResponse.getRecentPrice(),
            providerResponse.getAddress(),
            providerResponse.getLatitude(),
            providerResponse.getLongitude(),
            providerResponse.getHousingType()
        );
    }

    private static void validatePropertyId(final Long propertyId) {
        if (propertyId != null) {
            return;
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    private static void validateText(final String value) {
        if (value != null && !value.isBlank()) {
            return;
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    private static void validateRecentPrice(final long recentPrice) {
        if (recentPrice >= 0) {
            return;
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    private static void validateCoordinate(final BigDecimal coordinate) {
        if (coordinate != null) {
            return;
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    private static void validateHousingType(final HousingType housingType) {
        if (housingType != null) {
            return;
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }
}
