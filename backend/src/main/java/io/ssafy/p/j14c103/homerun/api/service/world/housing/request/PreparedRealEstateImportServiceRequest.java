package io.ssafy.p.j14c103.homerun.api.service.world.housing.request;

import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.world.housing.ContractTrap;
import io.ssafy.p.j14c103.homerun.domain.world.housing.GeocodingStatus;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.domain.world.housing.PropertyType;
import io.ssafy.p.j14c103.homerun.domain.world.housing.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;

@Getter
public class PreparedRealEstateImportServiceRequest {

    private final List<HousingRegionRow> housingRegions;
    private final List<HousingDistrictRow> housingDistricts;
    private final List<HousingLegalDongRow> housingLegalDongs;
    private final List<ApartmentTradeRawRow> apartmentTradeRaws;
    private final List<RealEstateGeocodeCacheRow> realEstateGeocodeCaches;
    private final List<RealEstatePropertyRow> realEstateProperties;

    private PreparedRealEstateImportServiceRequest(
        List<HousingRegionRow> housingRegions,
        List<HousingDistrictRow> housingDistricts,
        List<HousingLegalDongRow> housingLegalDongs,
        List<ApartmentTradeRawRow> apartmentTradeRaws,
        List<RealEstateGeocodeCacheRow> realEstateGeocodeCaches,
        List<RealEstatePropertyRow> realEstateProperties
    ) {
        this.housingRegions = List.copyOf(housingRegions);
        this.housingDistricts = List.copyOf(housingDistricts);
        this.housingLegalDongs = List.copyOf(housingLegalDongs);
        this.apartmentTradeRaws = List.copyOf(apartmentTradeRaws);
        this.realEstateGeocodeCaches = List.copyOf(realEstateGeocodeCaches);
        this.realEstateProperties = List.copyOf(realEstateProperties);
    }

    public static PreparedRealEstateImportServiceRequest of(
        List<HousingRegionRow> housingRegions,
        List<HousingDistrictRow> housingDistricts,
        List<HousingLegalDongRow> housingLegalDongs,
        List<ApartmentTradeRawRow> apartmentTradeRaws,
        List<RealEstateGeocodeCacheRow> realEstateGeocodeCaches,
        List<RealEstatePropertyRow> realEstateProperties
    ) {
        return new PreparedRealEstateImportServiceRequest(
            housingRegions,
            housingDistricts,
            housingLegalDongs,
            apartmentTradeRaws,
            realEstateGeocodeCaches,
            realEstateProperties
        );
    }

    @Getter
    public static final class HousingRegionRow {

        private final String regionCode;
        private final String regionName;

        private HousingRegionRow(String regionCode, String regionName) {
            this.regionCode = regionCode;
            this.regionName = regionName;
        }

        public static HousingRegionRow of(String regionCode, String regionName) {
            return new HousingRegionRow(regionCode, regionName);
        }
    }

    @Getter
    public static final class HousingDistrictRow {

        private final String districtCode;
        private final String regionCode;
        private final String districtName;
        private final String districtLegalDongCode;

        private HousingDistrictRow(
            String districtCode,
            String regionCode,
            String districtName,
            String districtLegalDongCode
        ) {
            this.districtCode = districtCode;
            this.regionCode = regionCode;
            this.districtName = districtName;
            this.districtLegalDongCode = districtLegalDongCode;
        }

        public static HousingDistrictRow of(
            String districtCode,
            String regionCode,
            String districtName,
            String districtLegalDongCode
        ) {
            return new HousingDistrictRow(
                districtCode,
                regionCode,
                districtName,
                districtLegalDongCode
            );
        }
    }

    @Getter
    public static final class HousingLegalDongRow {

        private final String legalDongCode;
        private final String parentLegalDongCode;
        private final String regionCode;
        private final String districtCode;
        private final String legalDongName;
        private final String fullAddressName;

        private HousingLegalDongRow(
            String legalDongCode,
            String parentLegalDongCode,
            String regionCode,
            String districtCode,
            String legalDongName,
            String fullAddressName
        ) {
            this.legalDongCode = legalDongCode;
            this.parentLegalDongCode = parentLegalDongCode;
            this.regionCode = regionCode;
            this.districtCode = districtCode;
            this.legalDongName = legalDongName;
            this.fullAddressName = fullAddressName;
        }

        public static HousingLegalDongRow of(
            String legalDongCode,
            String parentLegalDongCode,
            String regionCode,
            String districtCode,
            String legalDongName,
            String fullAddressName
        ) {
            return new HousingLegalDongRow(
                legalDongCode,
                parentLegalDongCode,
                regionCode,
                districtCode,
                legalDongName,
                fullAddressName
            );
        }
    }

    @Getter
    public static final class ApartmentTradeRawRow {

        private final String tradeKey;
        private final String districtCode;
        private final String legalDongName;
        private final String legalDongCode;
        private final String apartmentName;
        private final String jibun;
        private final LocalDate dealDate;
        private final Money dealAmount;
        private final BigDecimal exclusiveArea;
        private final int floor;
        private final int buildYear;
        private final boolean landLeasehold;

        private ApartmentTradeRawRow(
            String tradeKey,
            String districtCode,
            String legalDongName,
            String legalDongCode,
            String apartmentName,
            String jibun,
            LocalDate dealDate,
            Money dealAmount,
            BigDecimal exclusiveArea,
            int floor,
            int buildYear,
            boolean landLeasehold
        ) {
            this.tradeKey = tradeKey;
            this.districtCode = districtCode;
            this.legalDongName = legalDongName;
            this.legalDongCode = legalDongCode;
            this.apartmentName = apartmentName;
            this.jibun = jibun;
            this.dealDate = dealDate;
            this.dealAmount = dealAmount;
            this.exclusiveArea = exclusiveArea;
            this.floor = floor;
            this.buildYear = buildYear;
            this.landLeasehold = landLeasehold;
        }

        public static ApartmentTradeRawRow of(
            String tradeKey,
            String districtCode,
            String legalDongName,
            String legalDongCode,
            String apartmentName,
            String jibun,
            LocalDate dealDate,
            Money dealAmount,
            BigDecimal exclusiveArea,
            int floor,
            int buildYear,
            boolean landLeasehold
        ) {
            return new ApartmentTradeRawRow(
                tradeKey,
                districtCode,
                legalDongName,
                legalDongCode,
                apartmentName,
                jibun,
                dealDate,
                dealAmount,
                exclusiveArea,
                floor,
                buildYear,
                landLeasehold
            );
        }
    }

    @Getter
    public static final class RealEstateGeocodeCacheRow {

        private final String geocodingQuery;
        private final GeocodingStatus geocodingStatus;
        private final BigDecimal latitude;
        private final BigDecimal longitude;
        private final String resolvedRoadAddress;
        private final String resolvedJibunAddress;

        private RealEstateGeocodeCacheRow(
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

        public static RealEstateGeocodeCacheRow of(
            String geocodingQuery,
            GeocodingStatus geocodingStatus,
            BigDecimal latitude,
            BigDecimal longitude,
            String resolvedRoadAddress,
            String resolvedJibunAddress
        ) {
            return new RealEstateGeocodeCacheRow(
                geocodingQuery,
                geocodingStatus,
                latitude,
                longitude,
                resolvedRoadAddress,
                resolvedJibunAddress
            );
        }
    }

    @Getter
    public static final class RealEstatePropertyRow {

        private final String providerId;
        private final String propertyName;
        private final String address;
        private final String regionCode;
        private final String districtCode;
        private final String legalDongCode;
        private final Money basePrice;
        private final BigDecimal latitude;
        private final BigDecimal longitude;
        private final PropertyType propertyType;
        private final TransactionType transactionType;
        private final HousingType housingType;
        private final List<ContractTrap> contractTraps;

        private RealEstatePropertyRow(
            String providerId,
            String propertyName,
            String address,
            String regionCode,
            String districtCode,
            String legalDongCode,
            Money basePrice,
            BigDecimal latitude,
            BigDecimal longitude,
            PropertyType propertyType,
            TransactionType transactionType,
            HousingType housingType,
            List<ContractTrap> contractTraps
        ) {
            this.providerId = providerId;
            this.propertyName = propertyName;
            this.address = address;
            this.regionCode = regionCode;
            this.districtCode = districtCode;
            this.legalDongCode = legalDongCode;
            this.basePrice = basePrice;
            this.latitude = latitude;
            this.longitude = longitude;
            this.propertyType = propertyType;
            this.transactionType = transactionType;
            this.housingType = housingType;
            this.contractTraps = sanitizeContractTraps(contractTraps);
        }

        public static RealEstatePropertyRow of(
            String providerId,
            String propertyName,
            String address,
            String regionCode,
            String districtCode,
            String legalDongCode,
            Money basePrice,
            BigDecimal latitude,
            BigDecimal longitude,
            PropertyType propertyType,
            TransactionType transactionType,
            HousingType housingType,
            List<ContractTrap> contractTraps
        ) {
            return new RealEstatePropertyRow(
                providerId,
                propertyName,
                address,
                regionCode,
                districtCode,
                legalDongCode,
                basePrice,
                latitude,
                longitude,
                propertyType,
                transactionType,
                housingType,
                contractTraps
            );
        }

        private static List<ContractTrap> sanitizeContractTraps(List<ContractTrap> contractTraps) {
            if (contractTraps == null || contractTraps.isEmpty()) {
                return List.of();
            }
            return List.copyOf(contractTraps);
        }
    }
}
