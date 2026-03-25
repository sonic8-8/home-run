package io.ssafy.p.j14c103.homerun.api.service.world.housing.response;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;

@Getter
public class RealEstatePropertyListProviderResponse {

    private final List<PropertySummary> properties;

    private RealEstatePropertyListProviderResponse(final List<PropertySummary> properties) {
        if (properties == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }

        this.properties = List.copyOf(properties);
    }

    public static RealEstatePropertyListProviderResponse from(
        final List<PropertySummary> properties
    ) {
        return new RealEstatePropertyListProviderResponse(properties);
    }

    @Getter
    public static class PropertySummary {

        private final Long propertyId;
        private final String name;
        private final long recentPrice;
        private final BigDecimal latitude;
        private final BigDecimal longitude;

        private PropertySummary(
            final Long propertyId,
            final String name,
            final long recentPrice,
            final BigDecimal latitude,
            final BigDecimal longitude
        ) {
            validatePropertyId(propertyId);
            validateName(name);
            validatePrice(recentPrice);
            validateCoordinate(latitude);
            validateCoordinate(longitude);

            this.propertyId = propertyId;
            this.name = name;
            this.recentPrice = recentPrice;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public static PropertySummary of(
            final Long propertyId,
            final String name,
            final long recentPrice,
            final BigDecimal latitude,
            final BigDecimal longitude
        ) {
            return new PropertySummary(propertyId, name, recentPrice, latitude, longitude);
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

        private static void validateCoordinate(final BigDecimal coordinate) {
            if (coordinate == null) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
        }
    }
}
