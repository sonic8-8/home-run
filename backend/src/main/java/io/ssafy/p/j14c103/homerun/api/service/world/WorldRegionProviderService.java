package io.ssafy.p.j14c103.homerun.api.service.world;

import io.ssafy.p.j14c103.homerun.api.service.world.response.RegionsProviderResponse;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstatePropertyRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.WorldHousingSeedPolicy;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WorldRegionProviderService {

    private final RealEstatePropertyRepository realEstatePropertyRepository;
    private final WorldHousingSeedPolicy worldHousingSeedPolicy = new WorldHousingSeedPolicy();

    public RegionsProviderResponse getRegions() {
        final Set<String> availableRegionCodes = Set.copyOf(realEstatePropertyRepository.findDistinctRegionCodes());
        final List<RegionsProviderResponse.RegionItem> regions = worldHousingSeedPolicy.calculate()
            .regionSeeds()
            .stream()
            .filter(regionSeed -> availableRegionCodes.contains(regionSeed.regionCode()))
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
