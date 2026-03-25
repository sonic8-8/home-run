package io.ssafy.p.j14c103.homerun.api.service.world.response;

import java.util.List;
import lombok.Getter;

@Getter
public class RegionsProviderResponse {

    private final List<RegionItem> regions;

    private RegionsProviderResponse(final List<RegionItem> regions) {
        this.regions = List.copyOf(regions);
    }

    public static RegionsProviderResponse of(final List<RegionItem> regions) {
        return new RegionsProviderResponse(regions);
    }

    public static RegionsProviderResponse empty() {
        return new RegionsProviderResponse(List.of());
    }

    @Getter
    public static class RegionItem {

        private final String regionCode;
        private final String name;

        private RegionItem(
            final String regionCode,
            final String name
        ) {
            this.regionCode = regionCode;
            this.name = name;
        }

        public static RegionItem of(
            final String regionCode,
            final String name
        ) {
            return new RegionItem(regionCode, name);
        }
    }
}
