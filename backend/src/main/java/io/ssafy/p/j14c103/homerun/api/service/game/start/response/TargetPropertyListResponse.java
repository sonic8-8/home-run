package io.ssafy.p.j14c103.homerun.api.service.game.start.response;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class TargetPropertyListResponse {

    private final List<TargetPropertyResponse> properties;

    @Builder
    private TargetPropertyListResponse(final List<TargetPropertyResponse> properties) {
        validateProperties(properties);
        this.properties = List.copyOf(properties);
    }

    public static TargetPropertyListResponse from(final List<TargetPropertyResponse> properties) {
        return TargetPropertyListResponse.builder()
            .properties(properties)
            .build();
    }

    @Getter
    public static class TargetPropertyResponse {

        private final Integer propertyId;
        private final String name;
        private final long recentPrice;
        private final BigDecimal latitude;
        private final BigDecimal longitude;

        @Builder
        private TargetPropertyResponse(
            final Integer propertyId,
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

        public static TargetPropertyResponse of(
            final Integer propertyId,
            final String name,
            final long recentPrice,
            final BigDecimal latitude,
            final BigDecimal longitude
        ) {
            return TargetPropertyResponse.builder()
                .propertyId(propertyId)
                .name(name)
                .recentPrice(recentPrice)
                .latitude(latitude)
                .longitude(longitude)
                .build();
        }
    }

    private static void validateProperties(final List<TargetPropertyResponse> properties) {
        if (properties == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }

    private static void validatePropertyId(final Integer propertyId) {
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
