package io.ssafy.p.j14c103.homerun.api.service.game.start;

import java.math.BigDecimal;
import java.util.List;

public interface GameStartLocationProvider {

    List<RegionData> getRegions();

    List<DistrictData> getDistricts(String regionCode);

    List<TargetPropertyData> getTargetProperties(String regionCode, String districtCode);

    record RegionData(
        String regionCode,
        String name
    ) {
    }

    record DistrictData(
        String districtCode,
        String name
    ) {
    }

    record TargetPropertyData(
        Long propertyId,
        String name,
        long recentPrice,
        BigDecimal latitude,
        BigDecimal longitude
    ) {
    }
}
