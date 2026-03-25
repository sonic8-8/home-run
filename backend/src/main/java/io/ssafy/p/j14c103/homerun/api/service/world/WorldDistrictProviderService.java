package io.ssafy.p.j14c103.homerun.api.service.world;

import io.ssafy.p.j14c103.homerun.api.service.world.response.DistrictsProviderResponse;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingDistrictRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingRegionRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WorldDistrictProviderService {

    private final HousingRegionRepository housingRegionRepository;
    private final HousingDistrictRepository housingDistrictRepository;

    public DistrictsProviderResponse getDistricts(final String regionCode) {
        validateRegionCode(regionCode);

        final List<DistrictsProviderResponse.DistrictItem> districts = housingDistrictRepository
            .findAllByRegionCodeOrderByDistrictCodeAsc(regionCode).stream()
            .map(district -> DistrictsProviderResponse.DistrictItem.of(
                district.getDistrictCode(),
                district.getDistrictName()
            ))
            .toList();

        if (districts.isEmpty()) {
            return DistrictsProviderResponse.empty(regionCode);
        }

        return DistrictsProviderResponse.of(regionCode, districts);
    }

    private void validateRegionCode(final String regionCode) {
        if (regionCode == null || regionCode.isBlank()) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (!housingRegionRepository.existsById(regionCode)) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
