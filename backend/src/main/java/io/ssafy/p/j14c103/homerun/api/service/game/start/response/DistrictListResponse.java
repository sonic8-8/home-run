package io.ssafy.p.j14c103.homerun.api.service.game.start.response;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;

public record DistrictListResponse(
    String regionCode,
    List<DistrictResponse> districts
) {

    public DistrictListResponse {
        validateText(regionCode);
        if (districts == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
        districts = List.copyOf(districts);
    }

    public static DistrictListResponse of(
        final String regionCode,
        final List<DistrictResponse> districts
    ) {
        return new DistrictListResponse(regionCode, districts);
    }

    public record DistrictResponse(
        String districtCode,
        String name
    ) {

        public DistrictResponse {
            validateText(districtCode);
            validateText(name);
        }

        public static DistrictResponse of(
            final String districtCode,
            final String name
        ) {
            return new DistrictResponse(districtCode, name);
        }
    }

    private static void validateText(final String value) {
        if (value == null || value.isBlank()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }
}
