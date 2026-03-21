package io.ssafy.p.j14c103.homerun.api.service.world;

import io.ssafy.p.j14c103.homerun.api.service.world.response.RegionsProviderResponse;
import io.ssafy.p.j14c103.homerun.domain.world.housing.WorldHousingSeedPolicy;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class WorldRegionProviderService {

    private final WorldHousingSeedPolicy worldHousingSeedPolicy;

    public WorldRegionProviderService() {
        this(new WorldHousingSeedPolicy());
    }

    WorldRegionProviderService(final WorldHousingSeedPolicy worldHousingSeedPolicy) {
        this.worldHousingSeedPolicy = worldHousingSeedPolicy;
    }

    public RegionsProviderResponse getRegions() {
        final List<RegionsProviderResponse.RegionItem> regions = worldHousingSeedPolicy.calculate()
            .regionSeeds()
            .stream()
            .map(regionSeed -> RegionsProviderResponse.RegionItem.of(
                regionSeed.regionCode(),
                regionSeed.name()
            ))
            .toList();

        if (regions.isEmpty()) {
            return RegionsProviderResponse.empty();
        }

        return RegionsProviderResponse.of(regions);
    }
}
