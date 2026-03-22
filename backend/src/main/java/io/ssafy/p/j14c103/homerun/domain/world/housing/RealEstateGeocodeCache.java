package io.ssafy.p.j14c103.homerun.domain.world.housing;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "real_estate_geocode_caches")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RealEstateGeocodeCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "real_estate_geocode_cache_id")
    private Long realEstateGeocodeCacheId;

    @Column(name = "geocoding_query", unique = true, nullable = false)
    private String geocodingQuery;

    @Enumerated(EnumType.STRING)
    @Column(name = "geocoding_status", nullable = false)
    private GeocodingStatus geocodingStatus;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "resolved_road_address")
    private String resolvedRoadAddress;

    @Column(name = "resolved_jibun_address")
    private String resolvedJibunAddress;

    private RealEstateGeocodeCache(
        String geocodingQuery,
        GeocodingStatus geocodingStatus,
        BigDecimal latitude,
        BigDecimal longitude,
        String resolvedRoadAddress,
        String resolvedJibunAddress
    ) {
        this.geocodingQuery = geocodingQuery;
        this.geocodingStatus = geocodingStatus;
        this.latitude = latitude;
        this.longitude = longitude;
        this.resolvedRoadAddress = resolvedRoadAddress;
        this.resolvedJibunAddress = resolvedJibunAddress;
    }

    public static RealEstateGeocodeCache success(
        String geocodingQuery,
        BigDecimal latitude,
        BigDecimal longitude,
        String resolvedRoadAddress,
        String resolvedJibunAddress
    ) {
        return new RealEstateGeocodeCache(
            geocodingQuery,
            GeocodingStatus.SUCCESS,
            latitude,
            longitude,
            resolvedRoadAddress,
            resolvedJibunAddress
        );
    }

    public static RealEstateGeocodeCache noResult(String geocodingQuery) {
        return new RealEstateGeocodeCache(
            geocodingQuery,
            GeocodingStatus.NO_RESULT,
            null,
            null,
            null,
            null
        );
    }

    public void updateSuccess(
        BigDecimal latitude,
        BigDecimal longitude,
        String resolvedRoadAddress,
        String resolvedJibunAddress
    ) {
        this.geocodingStatus = GeocodingStatus.SUCCESS;
        this.latitude = latitude;
        this.longitude = longitude;
        this.resolvedRoadAddress = resolvedRoadAddress;
        this.resolvedJibunAddress = resolvedJibunAddress;
    }

    public void updateNoResult() {
        this.geocodingStatus = GeocodingStatus.NO_RESULT;
        this.latitude = null;
        this.longitude = null;
        this.resolvedRoadAddress = null;
        this.resolvedJibunAddress = null;
    }
}
