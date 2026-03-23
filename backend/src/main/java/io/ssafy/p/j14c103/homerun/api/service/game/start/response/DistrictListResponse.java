package io.ssafy.p.j14c103.homerun.api.service.game.start.response;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import lombok.Getter;

@Getter
public class DistrictListResponse {

    private final String regionCode;
    private final List<DistrictResponse> districts;

    private DistrictListResponse(
        final String regionCode,
        final List<DistrictResponse> districts
    ) {
        validateText(regionCode);
        validateDistricts(districts);
        this.regionCode = regionCode;
        this.districts = List.copyOf(districts);
    }

    public static DistrictListResponse of(
        final String regionCode,
        final List<DistrictResponse> districts
    ) {
        return new DistrictListResponse(regionCode, districts);
    }

    @Getter
    public static class DistrictResponse {

        private final String districtCode;
        private final String name;

        private DistrictResponse(
            final String districtCode,
            final String name
        ) {
            validateText(districtCode);
            validateText(name);
            this.districtCode = districtCode;
            this.name = name;
        }

        public static DistrictResponse of(
            final String districtCode,
            final String name
        ) {
            return new DistrictResponse(districtCode, name);
        }
    }

    private static void validateDistricts(final List<DistrictResponse> districts) {
        if (districts == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }

    private static void validateText(final String value) {
        if (value == null || value.isBlank()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }
}
