package io.ssafy.p.j14c103.homerun.api.service.world.response;

import java.util.List;
import lombok.Getter;

@Getter
public class DistrictsProviderResponse {

    private final String regionCode;
    private final List<DistrictItem> districts;

    private DistrictsProviderResponse(
        final String regionCode,
        final List<DistrictItem> districts
    ) {
        this.regionCode = regionCode;
        this.districts = List.copyOf(districts);
    }

    public static DistrictsProviderResponse of(
        final String regionCode,
        final List<DistrictItem> districts
    ) {
        return new DistrictsProviderResponse(regionCode, districts);
    }

    public static DistrictsProviderResponse empty(final String regionCode) {
        return new DistrictsProviderResponse(regionCode, List.of());
    }

    @Getter
    public static class DistrictItem {

        private final String districtCode;
        private final String name;

        private DistrictItem(
            final String districtCode,
            final String name
        ) {
            this.districtCode = districtCode;
            this.name = name;
        }

        public static DistrictItem of(
            final String districtCode,
            final String name
        ) {
            return new DistrictItem(districtCode, name);
        }
    }
}
