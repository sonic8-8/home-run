package io.ssafy.p.j14c103.homerun.api.service.game.start;

import io.ssafy.p.j14c103.homerun.api.service.game.start.response.DistrictListResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.start.response.RegionListResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.start.response.TargetPropertyListResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GameStartLocationService {

    private final GameStartLocationProvider gameStartLocationProvider;

    public RegionListResponse getRegions() {
        final List<RegionListResponse.RegionResponse> regions = gameStartLocationProvider.getRegions().stream()
            .map(region -> RegionListResponse.RegionResponse.of(region.regionCode(), region.name()))
            .toList();

        return RegionListResponse.from(regions);
    }

    public DistrictListResponse getDistricts(final String regionCode) {
        final List<DistrictListResponse.DistrictResponse> districts =
            gameStartLocationProvider.getDistricts(regionCode).stream()
                .map(district -> DistrictListResponse.DistrictResponse.of(
                    district.districtCode(),
                    district.name()
                ))
                .toList();

        return DistrictListResponse.of(regionCode, districts);
    }

    public TargetPropertyListResponse getTargetProperties(
        final String regionCode,
        final String districtCode
    ) {
        final List<TargetPropertyListResponse.TargetPropertyResponse> properties =
            gameStartLocationProvider.getTargetProperties(regionCode, districtCode).stream()
                .map(property -> TargetPropertyListResponse.TargetPropertyResponse.of(
                    property.propertyId(),
                    property.name(),
                    property.recentPrice(),
                    property.latitude(),
                    property.longitude()
                ))
                .toList();

        return TargetPropertyListResponse.from(properties);
    }
}
