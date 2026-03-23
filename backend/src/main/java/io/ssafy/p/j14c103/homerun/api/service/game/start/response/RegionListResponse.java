package io.ssafy.p.j14c103.homerun.api.service.game.start.response;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;

public record RegionListResponse(
    List<RegionResponse> regions
) {

    public RegionListResponse {
        if (regions == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
        regions = List.copyOf(regions);
    }

    public static RegionListResponse from(final List<RegionResponse> regions) {
        return new RegionListResponse(regions);
    }

    public record RegionResponse(
        String regionCode,
        String name
    ) {

        public RegionResponse {
            validateText(regionCode);
            validateText(name);
        }

        public static RegionResponse of(
            final String regionCode,
            final String name
        ) {
            return new RegionResponse(regionCode, name);
        }
    }

    private static void validateText(final String value) {
        if (value == null || value.isBlank()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }
}
