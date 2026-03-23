package io.ssafy.p.j14c103.homerun.api.service.game.start.response;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.util.List;

public record TargetPropertyListResponse(
    List<TargetPropertyResponse> properties
) {

    public TargetPropertyListResponse {
        if (properties == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
        properties = List.copyOf(properties);
    }

    public static TargetPropertyListResponse from(final List<TargetPropertyResponse> properties) {
        return new TargetPropertyListResponse(properties);
    }

    public record TargetPropertyResponse(
        Long propertyId,
        String name,
        long recentPrice,
        BigDecimal latitude,
        BigDecimal longitude
    ) {

        public TargetPropertyResponse {
            if (propertyId == null) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
            validateText(name);
            if (recentPrice < 0) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
        }

        public static TargetPropertyResponse of(
            final Long propertyId,
            final String name,
            final long recentPrice,
            final BigDecimal latitude,
            final BigDecimal longitude
        ) {
            return new TargetPropertyResponse(propertyId, name, recentPrice, latitude, longitude);
        }
    }

    private static void validateText(final String value) {
        if (value == null || value.isBlank()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }
}
