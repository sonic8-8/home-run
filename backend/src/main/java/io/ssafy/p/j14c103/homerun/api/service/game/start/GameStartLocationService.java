package io.ssafy.p.j14c103.homerun.api.service.game.start;

import io.ssafy.p.j14c103.homerun.api.service.game.start.response.DistrictListResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.start.response.RegionListResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.start.response.TargetPropertyListResponse;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingDistrict;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingDistrictRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingRegionRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateProperty;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstatePropertyRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GameStartLocationService {

    private final HousingRegionRepository housingRegionRepository;
    private final HousingDistrictRepository housingDistrictRepository;
    private final RealEstatePropertyRepository realEstatePropertyRepository;

    public RegionListResponse getRegions() {
        final List<RegionListResponse.RegionResponse> regions =
            housingRegionRepository.findAllByOrderByRegionCodeAsc().stream()
                .map(region -> RegionListResponse.RegionResponse.of(
                    region.getRegionCode(),
                    region.getRegionName()
                ))
                .toList();

        return RegionListResponse.from(regions);
    }

    public DistrictListResponse getDistricts(final String regionCode) {
        validateRegion(regionCode);

        final List<DistrictListResponse.DistrictResponse> districts =
            housingDistrictRepository.findAllByRegionCodeOrderByDistrictCodeAsc(regionCode).stream()
                .map(district -> DistrictListResponse.DistrictResponse.of(
                    district.getDistrictCode(),
                    district.getDistrictName()
                ))
                .toList();

        return DistrictListResponse.of(regionCode, districts);
    }

    public TargetPropertyListResponse getTargetProperties(
        final String regionCode,
        final String districtCode
    ) {
        validateRegion(regionCode);
        validateDistrict(regionCode, districtCode);

        final List<TargetPropertyListResponse.TargetPropertyResponse> properties =
            realEstatePropertyRepository.findAllByRegionCodeAndDistrictCodeOrderByPropertyIdAsc(
                    regionCode,
                    districtCode
                ).stream()
                .map(property -> TargetPropertyListResponse.TargetPropertyResponse.of(
                    property.getPropertyId(),
                    property.getPropertyName(),
                    extractRecentPrice(property),
                    property.getLatitude(),
                    property.getLongitude()
                ))
                .toList();

        return TargetPropertyListResponse.from(properties);
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

    private long extractRecentPrice(final RealEstateProperty property) {
        if (property.getBasePrice() != null) {
            return property.getBasePrice().getAmount().longValue();
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }
}
