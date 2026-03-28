package io.ssafy.p.j14c103.homerun.api.service.world.housing;

import io.ssafy.p.j14c103.homerun.api.service.world.housing.request.PreparedRealEstateImportServiceRequest;
import io.ssafy.p.j14c103.homerun.config.ConditionalOnRealEstateImportEnabled;
import io.ssafy.p.j14c103.homerun.domain.world.housing.GeocodingStatus;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingDistrict;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingDistrictRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingLegalDong;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingLegalDongRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingRegion;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingRegionRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateGeocodeCache;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateGeocodeCacheRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateProperty;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstatePropertyRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.trade.ApartmentTradeRaw;
import io.ssafy.p.j14c103.homerun.domain.world.housing.trade.ApartmentTradeRawRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnRealEstateImportEnabled
@RequiredArgsConstructor
public class JpaRealEstateImportPersistenceService {

    private final HousingRegionRepository housingRegionRepository;
    private final HousingDistrictRepository housingDistrictRepository;
    private final HousingLegalDongRepository housingLegalDongRepository;
    private final ApartmentTradeRawRepository apartmentTradeRawRepository;
    private final RealEstateGeocodeCacheRepository realEstateGeocodeCacheRepository;
    private final RealEstatePropertyRepository realEstatePropertyRepository;

    public void persist(PreparedRealEstateImportServiceRequest preparedImport) {
        upsertHousingRegions(preparedImport);
        upsertHousingDistricts(preparedImport);
        upsertHousingLegalDongs(preparedImport);
        upsertApartmentTradeRaws(preparedImport);
        upsertGeocodeCaches(preparedImport);
        upsertRealEstateProperties(preparedImport);
    }

    private void upsertHousingRegions(PreparedRealEstateImportServiceRequest preparedImport) {
        for (PreparedRealEstateImportServiceRequest.HousingRegionRow row : preparedImport.getHousingRegions()) {
            housingRegionRepository.findById(row.getRegionCode())
                .ifPresentOrElse(
                    existing -> existing.update(row.getRegionName()),
                    () -> housingRegionRepository.save(
                        HousingRegion.create(
                            row.getRegionCode(),
                            row.getRegionName()
                        )
                    )
                );
        }
    }

    private void upsertHousingDistricts(PreparedRealEstateImportServiceRequest preparedImport) {
        for (PreparedRealEstateImportServiceRequest.HousingDistrictRow row : preparedImport.getHousingDistricts()) {
            housingDistrictRepository.findById(row.getDistrictCode())
                .ifPresentOrElse(
                    existing -> existing.update(
                        row.getRegionCode(),
                        row.getDistrictName(),
                        row.getDistrictLegalDongCode()
                    ),
                    () -> housingDistrictRepository.save(
                        HousingDistrict.create(
                            row.getDistrictCode(),
                            row.getRegionCode(),
                            row.getDistrictName(),
                            row.getDistrictLegalDongCode()
                        )
                    )
                );
        }
    }

    private void upsertHousingLegalDongs(PreparedRealEstateImportServiceRequest preparedImport) {
        for (PreparedRealEstateImportServiceRequest.HousingLegalDongRow row : preparedImport.getHousingLegalDongs()) {
            housingLegalDongRepository.findById(row.getLegalDongCode())
                .ifPresentOrElse(
                    existing -> existing.update(
                        row.getParentLegalDongCode(),
                        row.getRegionCode(),
                        row.getDistrictCode(),
                        row.getLegalDongName(),
                        row.getFullAddressName()
                    ),
                    () -> housingLegalDongRepository.save(
                        HousingLegalDong.create(
                            row.getLegalDongCode(),
                            row.getParentLegalDongCode(),
                            row.getRegionCode(),
                            row.getDistrictCode(),
                            row.getLegalDongName(),
                            row.getFullAddressName()
                        )
                    )
                );
        }
    }

    private void upsertApartmentTradeRaws(PreparedRealEstateImportServiceRequest preparedImport) {
        for (PreparedRealEstateImportServiceRequest.ApartmentTradeRawRow row : preparedImport.getApartmentTradeRaws()) {
            if (apartmentTradeRawRepository.findByTradeKey(row.getTradeKey()).isPresent()) {
                continue;
            }

            apartmentTradeRawRepository.save(
                ApartmentTradeRaw.create(
                    row.getTradeKey(),
                    row.getDistrictCode(),
                    row.getLegalDongName(),
                    row.getLegalDongCode(),
                    row.getApartmentName(),
                    row.getJibun(),
                    row.getDealDate(),
                    row.getDealAmount(),
                    row.getExclusiveArea(),
                    row.getFloor(),
                    row.getBuildYear(),
                    row.isLandLeasehold()
                )
            );
        }
    }

    private void upsertGeocodeCaches(PreparedRealEstateImportServiceRequest preparedImport) {
        for (PreparedRealEstateImportServiceRequest.RealEstateGeocodeCacheRow row :
            preparedImport.getRealEstateGeocodeCaches()) {
            realEstateGeocodeCacheRepository.findByGeocodingQuery(row.getGeocodingQuery())
                .ifPresentOrElse(
                    existing -> updateGeocodeCache(existing, row),
                    () -> realEstateGeocodeCacheRepository.save(createGeocodeCache(row))
                );
        }
    }

    private void upsertRealEstateProperties(PreparedRealEstateImportServiceRequest preparedImport) {
        for (PreparedRealEstateImportServiceRequest.RealEstatePropertyRow row :
            preparedImport.getRealEstateProperties()) {
            realEstatePropertyRepository.findByProviderId(row.getProviderId())
                .ifPresentOrElse(
                    existing -> existing.updateFromImport(
                        row.getPropertyName(),
                        row.getAddress(),
                        row.getRegionCode(),
                        row.getDistrictCode(),
                        row.getLegalDongCode(),
                        row.getBasePrice(),
                        resolveLatitude(row, existing),
                        resolveLongitude(row, existing),
                        row.getPropertyType(),
                        row.getTransactionType(),
                        row.getHousingType(),
                        row.getContractTraps()
                    ),
                    () -> realEstatePropertyRepository.save(
                        RealEstateProperty.create(
                            row.getProviderId(),
                            row.getPropertyName(),
                            row.getAddress(),
                            row.getRegionCode(),
                            row.getDistrictCode(),
                            row.getLegalDongCode(),
                            row.getBasePrice(),
                            row.getLatitude(),
                            row.getLongitude(),
                            row.getPropertyType(),
                            row.getTransactionType(),
                            row.getHousingType(),
                            row.getContractTraps()
                        )
                    )
                );
        }
    }

    private void updateGeocodeCache(
        RealEstateGeocodeCache existing,
        PreparedRealEstateImportServiceRequest.RealEstateGeocodeCacheRow row
    ) {
        if (row.getGeocodingStatus() == GeocodingStatus.SUCCESS) {
            existing.updateSuccess(
                row.getLatitude(),
                row.getLongitude(),
                row.getResolvedRoadAddress(),
                row.getResolvedJibunAddress()
            );
            return;
        }
        existing.updateNoResult();
    }

    private RealEstateGeocodeCache createGeocodeCache(
        PreparedRealEstateImportServiceRequest.RealEstateGeocodeCacheRow row
    ) {
        if (row.getGeocodingStatus() == GeocodingStatus.SUCCESS) {
            return RealEstateGeocodeCache.success(
                row.getGeocodingQuery(),
                row.getLatitude(),
                row.getLongitude(),
                row.getResolvedRoadAddress(),
                row.getResolvedJibunAddress()
            );
        }
        return RealEstateGeocodeCache.noResult(row.getGeocodingQuery());
    }

    private BigDecimal resolveLatitude(
        PreparedRealEstateImportServiceRequest.RealEstatePropertyRow row,
        RealEstateProperty existing
    ) {
        if (row.getLatitude() != null) {
            return row.getLatitude();
        }
        return existing.getLatitude();
    }

    private BigDecimal resolveLongitude(
        PreparedRealEstateImportServiceRequest.RealEstatePropertyRow row,
        RealEstateProperty existing
    ) {
        if (row.getLongitude() != null) {
            return row.getLongitude();
        }
        return existing.getLongitude();
    }
}
