package io.ssafy.p.j14c103.homerun.api.service.world;

import io.ssafy.p.j14c103.homerun.api.service.world.response.DistrictsProviderResponse;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstatePropertyRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.WorldHousingSeedPolicy;
import io.ssafy.p.j14c103.homerun.domain.world.housing.WorldHousingSeedPolicy.DistrictSeed;
import io.ssafy.p.j14c103.homerun.domain.world.housing.WorldHousingSeedPolicy.WorldHousingSeedPlan;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WorldDistrictProviderService {

    private final RealEstatePropertyRepository realEstatePropertyRepository;
    private final WorldHousingSeedPolicy worldHousingSeedPolicy = new WorldHousingSeedPolicy();

    public DistrictsProviderResponse getDistricts(final String regionCode) {
        final WorldHousingSeedPlan seedPlan = worldHousingSeedPolicy.calculate();
        validateRegionCode(regionCode, seedPlan);
        final Set<String> availableDistrictCodes = Set.copyOf(
            realEstatePropertyRepository.findDistinctDistrictCodesByRegionCode(regionCode)
        );

        final List<DistrictsProviderResponse.DistrictItem> districts = seedPlan.districtSeeds().stream()
            .filter(districtSeed -> districtSeed.regionCode().equals(regionCode))
            .filter(districtSeed -> availableDistrictCodes.contains(districtSeed.districtCode()))
            .map(this::toDistrictItem)
            .toList();

        if (districts.isEmpty()) {
            return DistrictsProviderResponse.empty(regionCode);
        }

        return DistrictsProviderResponse.of(regionCode, districts);
    }

    private void validateRegionCode(
        final String regionCode,
        final WorldHousingSeedPlan seedPlan
    ) {
        if (regionCode == null || regionCode.isBlank()) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }

        final boolean exists = seedPlan.regionSeeds().stream()
            .anyMatch(regionSeed -> regionSeed.regionCode().equals(regionCode));

        if (!exists) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private DistrictsProviderResponse.DistrictItem toDistrictItem(final DistrictSeed districtSeed) {
        return DistrictsProviderResponse.DistrictItem.of(
            districtSeed.districtCode(),
            districtSeed.name()
        );
    }
}
