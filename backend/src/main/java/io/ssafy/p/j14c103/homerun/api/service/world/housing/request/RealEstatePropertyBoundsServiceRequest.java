package io.ssafy.p.j14c103.homerun.api.service.world.housing.request;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RealEstatePropertyBoundsServiceRequest {

    private static final BigDecimal MIN_LATITUDE = BigDecimal.valueOf(-90);
    private static final BigDecimal MAX_LATITUDE = BigDecimal.valueOf(90);
    private static final BigDecimal MIN_LONGITUDE = BigDecimal.valueOf(-180);
    private static final BigDecimal MAX_LONGITUDE = BigDecimal.valueOf(180);

    private BigDecimal minLatitude;

    private BigDecimal minLongitude;

    private BigDecimal maxLatitude;

    private BigDecimal maxLongitude;

    @Builder(access = AccessLevel.PRIVATE)
    private RealEstatePropertyBoundsServiceRequest(
        final BigDecimal minLatitude,
        final BigDecimal minLongitude,
        final BigDecimal maxLatitude,
        final BigDecimal maxLongitude
    ) {
        validateCoordinates(minLatitude, minLongitude, maxLatitude, maxLongitude);

        this.minLatitude = minLatitude;
        this.minLongitude = minLongitude;
        this.maxLatitude = maxLatitude;
        this.maxLongitude = maxLongitude;
    }

    public static RealEstatePropertyBoundsServiceRequest of(
        final BigDecimal minLatitude,
        final BigDecimal minLongitude,
        final BigDecimal maxLatitude,
        final BigDecimal maxLongitude
    ) {
        return RealEstatePropertyBoundsServiceRequest.builder()
            .minLatitude(minLatitude)
            .minLongitude(minLongitude)
            .maxLatitude(maxLatitude)
            .maxLongitude(maxLongitude)
            .build();
    }

    public BigDecimal minLatitude() {
        return minLatitude;
    }

    public BigDecimal minLongitude() {
        return minLongitude;
    }

    public BigDecimal maxLatitude() {
        return maxLatitude;
    }

    public BigDecimal maxLongitude() {
        return maxLongitude;
    }

    private void validateCoordinates(
        final BigDecimal minLatitude,
        final BigDecimal minLongitude,
        final BigDecimal maxLatitude,
        final BigDecimal maxLongitude
    ) {
        if (minLatitude == null || minLongitude == null || maxLatitude == null || maxLongitude == null) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (minLatitude.compareTo(maxLatitude) > 0 || minLongitude.compareTo(maxLongitude) > 0) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }

        validateLatitude(minLatitude);
        validateLatitude(maxLatitude);
        validateLongitude(minLongitude);
        validateLongitude(maxLongitude);
    }

    private void validateLatitude(final BigDecimal latitude) {
        if (latitude.compareTo(MIN_LATITUDE) < 0 || latitude.compareTo(MAX_LATITUDE) > 0) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void validateLongitude(final BigDecimal longitude) {
        if (longitude.compareTo(MIN_LONGITUDE) < 0 || longitude.compareTo(MAX_LONGITUDE) > 0) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
