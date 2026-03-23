package io.ssafy.p.j14c103.homerun.api.service.game.start.response;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import lombok.Getter;

@Getter
public class RegionListResponse {

    private final List<RegionResponse> regions;

    private RegionListResponse(final List<RegionResponse> regions) {
        validateRegions(regions);
        this.regions = List.copyOf(regions);
    }

    public static RegionListResponse from(final List<RegionResponse> regions) {
        return new RegionListResponse(regions);
    }

    @Getter
    public static class RegionResponse {

        private final String regionCode;
        private final String name;

        private RegionResponse(
            final String regionCode,
            final String name
        ) {
            validateText(regionCode);
            validateText(name);
            this.regionCode = regionCode;
            this.name = name;
        }

        public static RegionResponse of(
            final String regionCode,
            final String name
        ) {
            return new RegionResponse(regionCode, name);
        }
    }

    private static void validateRegions(final List<RegionResponse> regions) {
        if (regions == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }

    private static void validateText(final String value) {
        if (value == null || value.isBlank()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }
}
