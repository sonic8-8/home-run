package io.ssafy.p.j14c103.homerun.api.service.world;

import io.ssafy.p.j14c103.homerun.api.service.world.response.TargetPropertiesProviderResponse;
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
public class TargetPropertyProviderService {

    private final HousingRegionRepository housingRegionRepository;
    private final HousingDistrictRepository housingDistrictRepository;
    private final RealEstatePropertyRepository realEstatePropertyRepository;

    public TargetPropertiesProviderResponse getTargetProperties(
        final String regionCode,
        final String districtCode
    ) {
        validateRegion(regionCode);
        validateDistrict(regionCode, districtCode);

        final List<TargetPropertiesProviderResponse.TargetPropertyItem> properties =
            realEstatePropertyRepository.findAllByRegionCodeAndDistrictCodeOrderByPropertyIdAsc(
                    regionCode,
                    districtCode
                ).stream()
                .map(property -> TargetPropertiesProviderResponse.TargetPropertyItem.of(
                    property.getPropertyId(),
                    property.getPropertyName(),
                    extractRecentPrice(property),
                    property.getLatitude(),
                    property.getLongitude()
                ))
                .toList();

        return TargetPropertiesProviderResponse.from(properties);
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
