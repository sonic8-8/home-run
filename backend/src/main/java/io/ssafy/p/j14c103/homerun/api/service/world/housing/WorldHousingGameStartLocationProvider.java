package io.ssafy.p.j14c103.homerun.api.service.world.housing;

import io.ssafy.p.j14c103.homerun.api.service.game.start.GameStartLocationProvider;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingDistrict;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingDistrictRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingRegionRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateProperty;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstatePropertyRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WorldHousingGameStartLocationProvider implements GameStartLocationProvider {

    private final HousingRegionRepository housingRegionRepository;
    private final HousingDistrictRepository housingDistrictRepository;
    private final RealEstatePropertyRepository realEstatePropertyRepository;

    @Override
    public List<RegionData> getRegions() {
        return housingRegionRepository.findAllByOrderByRegionCodeAsc().stream()
            .map(region -> new RegionData(region.getRegionCode(), region.getRegionName()))
            .toList();
    }

    @Override
    public List<DistrictData> getDistricts(final String regionCode) {
        validateRegion(regionCode);

        return housingDistrictRepository.findAllByRegionCodeOrderByDistrictCodeAsc(regionCode).stream()
            .map(district -> new DistrictData(district.getDistrictCode(), district.getDistrictName()))
            .toList();
    }

    @Override
    public List<TargetPropertyData> getTargetProperties(
        final String regionCode,
        final String districtCode
    ) {
        validateRegion(regionCode);
        validateDistrict(regionCode, districtCode);

        return realEstatePropertyRepository.findAllByRegionCodeAndDistrictCodeOrderByPropertyIdAsc(
                regionCode,
                districtCode
            ).stream()
            .map(this::toTargetPropertyData)
            .toList();
    }

    private void validateRegion(final String regionCode) {
        if (housingRegionRepository.existsById(regionCode)) {
            return;
        }

        throw new HomerunException(ErrorCode.HOUSING_REGION_NOT_FOUND);
    }

    private void validateDistrict(final String regionCode, final String districtCode) {
        final HousingDistrict district = housingDistrictRepository.findById(districtCode)
            .orElseThrow(() -> new HomerunException(ErrorCode.HOUSING_DISTRICT_NOT_FOUND));

        if (district.getRegionCode().equals(regionCode)) {
            return;
        }

        throw new HomerunException(ErrorCode.HOUSING_DISTRICT_NOT_FOUND);
    }

    private TargetPropertyData toTargetPropertyData(final RealEstateProperty property) {
        return new TargetPropertyData(
            property.getPropertyId(),
            property.getPropertyName(),
            extractRecentPrice(property),
            property.getLatitude(),
            property.getLongitude()
        );
    }

    private long extractRecentPrice(final RealEstateProperty property) {
        if (property.getBasePrice() != null) {
            return property.getBasePrice().getAmount().longValue();
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }
}
