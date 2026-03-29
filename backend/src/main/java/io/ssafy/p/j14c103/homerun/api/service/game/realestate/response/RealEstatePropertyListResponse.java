package io.ssafy.p.j14c103.homerun.api.service.game.realestate.response;

import io.ssafy.p.j14c103.homerun.api.service.world.housing.response.RealEstatePropertyListProviderResponse;
import io.ssafy.p.j14c103.homerun.api.service.world.response.TargetPropertiesProviderResponse;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;

@Getter
public class RealEstatePropertyListResponse {

    private final List<PropertySummaryResponse> properties;

    private RealEstatePropertyListResponse(final List<PropertySummaryResponse> properties) {
        validateProperties(properties);
        this.properties = List.copyOf(properties);
    }

    public static RealEstatePropertyListResponse from(final List<PropertySummaryResponse> properties) {
        return new RealEstatePropertyListResponse(properties);
    }

    public static RealEstatePropertyListResponse from(
        final RealEstatePropertyListProviderResponse providerResponse
    ) {
        return from(
            providerResponse.getProperties().stream()
                .map(property -> PropertySummaryResponse.of(
                    property.getPropertyId(),
                    property.getName(),
                    property.getRecentPrice(),
                    property.getLatitude(),
                    property.getLongitude()
                ))
                .toList()
        );
    }

    public static RealEstatePropertyListResponse from(
        final TargetPropertiesProviderResponse providerResponse
    ) {
        return from(
            providerResponse.getProperties().stream()
                .map(property -> PropertySummaryResponse.of(
                    property.getPropertyId(),
                    property.getName(),
                    property.getRecentPrice(),
                    property.getLatitude(),
                    property.getLongitude()
                ))
                .toList()
        );
    }

    private static void validateProperties(final List<PropertySummaryResponse> properties) {
        if (properties != null) {
            return;
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    @Getter
    public static class PropertySummaryResponse {

        private final Long propertyId;
        private final String name;
        private final long recentPrice;
        private final BigDecimal latitude;
        private final BigDecimal longitude;

        private PropertySummaryResponse(
            final Long propertyId,
            final String name,
            final long recentPrice,
            final BigDecimal latitude,
            final BigDecimal longitude
        ) {
            validatePropertyId(propertyId);
            validateText(name);
            validateRecentPrice(recentPrice);
            validateCoordinate(latitude);
            validateCoordinate(longitude);

            this.propertyId = propertyId;
            this.name = name;
            this.recentPrice = recentPrice;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public static PropertySummaryResponse of(
            final Long propertyId,
            final String name,
            final long recentPrice,
            final BigDecimal latitude,
            final BigDecimal longitude
        ) {
            return new PropertySummaryResponse(propertyId, name, recentPrice, latitude, longitude);
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
    }
}
