package io.ssafy.p.j14c103.homerun.api.service.world.housing;

import io.ssafy.p.j14c103.homerun.api.service.world.housing.request.RealEstatePropertyBoundsServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.response.RealEstatePropertyDetailProviderResponse;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.response.RealEstatePropertyListProviderResponse;
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
public class WorldRealEstatePropertyProviderService {

    private final RealEstatePropertyRepository realEstatePropertyRepository;

    public RealEstatePropertyListProviderResponse getPropertiesInBounds(
        final RealEstatePropertyBoundsServiceRequest request
    ) {
        validateRequest(request);

        final List<RealEstatePropertyListProviderResponse.PropertySummary> properties =
            realEstatePropertyRepository
                .findAllByLatitudeBetweenAndLongitudeBetweenOrderByPropertyIdAsc(
                    request.minLatitude(),
                    request.maxLatitude(),
                    request.minLongitude(),
                    request.maxLongitude()
                )
                .stream()
                .map(this::toPropertySummary)
                .toList();

        return RealEstatePropertyListProviderResponse.from(properties);
    }

    public RealEstatePropertyDetailProviderResponse getPropertyDetail(final Long propertyId) {
        validatePropertyId(propertyId);

        final RealEstateProperty property = realEstatePropertyRepository.findById(propertyId)
            .orElseThrow(() -> new HomerunException(ErrorCode.HOUSING_PROPERTY_NOT_FOUND));

        return RealEstatePropertyDetailProviderResponse.of(
            property.getPropertyId(),
            property.getPropertyName(),
            extractRecentPrice(property),
            property.getAddress(),
            property.getLatitude(),
            property.getLongitude(),
            property.getHousingType()
        );
    }

    private void validateRequest(final RealEstatePropertyBoundsServiceRequest request) {
        if (request == null) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void validatePropertyId(final Long propertyId) {
        if (propertyId == null || propertyId <= 0) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private RealEstatePropertyListProviderResponse.PropertySummary toPropertySummary(
        final RealEstateProperty property
    ) {
        return RealEstatePropertyListProviderResponse.PropertySummary.of(
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
