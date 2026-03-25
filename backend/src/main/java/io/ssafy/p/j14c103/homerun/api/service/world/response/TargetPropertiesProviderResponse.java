package io.ssafy.p.j14c103.homerun.api.service.world.response;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class TargetPropertiesProviderResponse {

    private final List<TargetPropertyItem> properties;

    @Builder
    private TargetPropertiesProviderResponse(final List<TargetPropertyItem> properties) {
        validateProperties(properties);
        this.properties = List.copyOf(properties);
    }

    public static TargetPropertiesProviderResponse from(final List<TargetPropertyItem> properties) {
        return TargetPropertiesProviderResponse.builder()
            .properties(properties)
            .build();
    }

    @Getter
    public static class TargetPropertyItem {

        private final Long propertyId;
        private final String name;
        private final long recentPrice;
        private final BigDecimal latitude;
        private final BigDecimal longitude;

        @Builder
        private TargetPropertyItem(
            final Long propertyId,
            final String name,
            final long recentPrice,
            final BigDecimal latitude,
            final BigDecimal longitude
        ) {
            validatePropertyId(propertyId);
            validateText(name);
            validateRecentPrice(recentPrice);
            this.propertyId = propertyId;
            this.name = name;
            this.recentPrice = recentPrice;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public static TargetPropertyItem of(
            final Long propertyId,
            final String name,
            final long recentPrice,
            final BigDecimal latitude,
            final BigDecimal longitude
        ) {
            return TargetPropertyItem.builder()
                .propertyId(propertyId)
                .name(name)
                .recentPrice(recentPrice)
                .latitude(latitude)
                .longitude(longitude)
                .build();
        }
    }

    private static void validateProperties(final List<TargetPropertyItem> properties) {
        if (properties == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }

    private static void validatePropertyId(final Long propertyId) {
        if (propertyId == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }

    private static void validateRecentPrice(final long recentPrice) {
        if (recentPrice < 0) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }

    private static void validateText(final String value) {
        if (value == null || value.isBlank()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }
}
