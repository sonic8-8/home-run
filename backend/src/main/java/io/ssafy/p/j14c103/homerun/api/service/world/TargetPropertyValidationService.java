package io.ssafy.p.j14c103.homerun.api.service.world;

import io.ssafy.p.j14c103.homerun.api.service.world.response.TargetPropertyValidationResponse;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingDistrict;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingDistrictRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingRegionRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateProperty;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstatePropertyRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TargetPropertyValidationService {

    private final HousingRegionRepository housingRegionRepository;
    private final HousingDistrictRepository housingDistrictRepository;
    private final RealEstatePropertyRepository realEstatePropertyRepository;

    public TargetPropertyValidationResponse validateTargetProperty(
        final String regionCode,
        final String districtCode,
        final Integer propertyId
    ) {
        validateRegion(regionCode);
        validateDistrict(regionCode, districtCode);

        final RealEstateProperty property = realEstatePropertyRepository.findById(propertyId)
            .orElseThrow(() -> new HomerunException(ErrorCode.HOUSING_PROPERTY_NOT_FOUND));

        validatePropertyLocation(regionCode, districtCode, property);

        return TargetPropertyValidationResponse.of(
            property.getPropertyId(),
            extractPriceSnapshot(property),
            property.getHousingType()
        );
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

    private void validatePropertyLocation(
        final String regionCode,
        final String districtCode,
        final RealEstateProperty property
    ) {
        if (property.getRegionCode().equals(regionCode) && property.getDistrictCode().equals(districtCode)) {
            return;
        }

        throw new HomerunException(ErrorCode.HOUSING_PROPERTY_NOT_FOUND);
    }

    private long extractPriceSnapshot(final RealEstateProperty property) {
        if (property.getBasePrice() != null) {
            return property.getBasePrice().getAmount().longValue();
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }
}
