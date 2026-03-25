package io.ssafy.p.j14c103.homerun.api.service.world;

import io.ssafy.p.j14c103.homerun.api.service.world.response.RegionsProviderResponse;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingRegionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WorldRegionProviderService {

    private final HousingRegionRepository housingRegionRepository;

    public RegionsProviderResponse getRegions() {
        final List<RegionsProviderResponse.RegionItem> regions = housingRegionRepository
            .findAllByOrderByRegionCodeAsc().stream()
            .map(region -> RegionsProviderResponse.RegionItem.of(
                region.getRegionCode(),
                region.getRegionName()
            ))
            .toList();

        if (regions.isEmpty()) {
            return RegionsProviderResponse.empty();
        }

        return RegionsProviderResponse.of(regions);
    }
}
