package io.ssafy.p.j14c103.homerun.api.service.world.housing;

import io.ssafy.p.j14c103.homerun.api.service.world.housing.request.PreparedRealEstateImportServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.request.RealEstateMasterImportRequest;
import io.ssafy.p.j14c103.homerun.client.naver.NaverGeocodingClient;
import io.ssafy.p.j14c103.homerun.client.publicdata.realestate.ApartmentTradeResponseParser;
import io.ssafy.p.j14c103.homerun.client.publicdata.realestate.LegalDongCodeResponseParser;
import io.ssafy.p.j14c103.homerun.config.ConditionalOnRealEstateImportEnabled;
import io.ssafy.p.j14c103.homerun.domain.world.housing.GeocodingStatus;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingDistrict;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingLegalDong;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingLegalDongRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingRegion;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.domain.world.housing.PropertyType;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateGeocodeCache;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateGeocodeCacheRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.TransactionType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnRealEstateImportEnabled
@Slf4j
@RequiredArgsConstructor
public class RealEstateImportPreparationService {

    private final LegalDongCodeResponseParser legalDongCodeResponseParser;
    private final ApartmentTradeResponseParser apartmentTradeResponseParser;
    private final HousingLegalDongRepository housingLegalDongRepository;
    private final RealEstateGeocodeCacheRepository realEstateGeocodeCacheRepository;
    private final NaverGeocodingClient naverGeocodingClient;

    public PreparedRealEstateImportServiceRequest prepareImport(RealEstateMasterImportRequest request) {
        List<LegalDongCodeResponseParser.LegalDongCodeRow> legalDongRows =
            legalDongCodeResponseParser.parse(request.getLegalDongXml());
        MasterRows masterRows = buildMasterRows(legalDongRows);

        Map<String, Optional<HousingLegalDong>> existingLegalDongLookupCache = new HashMap<>();
        Map<String, PreparedRealEstateImportServiceRequest.ApartmentTradeRawRow> apartmentTradeRawRows =
            new LinkedHashMap<>();
        Map<String, List<ResolvedTradeCandidate>> candidatesByProviderId = new LinkedHashMap<>();
        GeocodingPreparationContext geocodingContext = new GeocodingPreparationContext();

        for (String apartmentTradeResponse : resolveApartmentTradeResponses(request)) {
            collectTradeRows(
                apartmentTradeResponse,
                masterRows,
                existingLegalDongLookupCache,
                apartmentTradeRawRows,
                candidatesByProviderId
            );
        }

        List<PreparedRealEstateImportServiceRequest.RealEstatePropertyRow> realEstatePropertyRows =
            buildRepresentativeProperties(candidatesByProviderId, geocodingContext);

        return PreparedRealEstateImportServiceRequest.of(
            masterRows.housingRegions(),
            masterRows.housingDistricts(),
            masterRows.housingLegalDongs(),
            new ArrayList<>(apartmentTradeRawRows.values()),
            geocodingContext.pendingCacheRows(),
            realEstatePropertyRows
        );
    }

    private void collectTradeRows(
        String apartmentTradeResponse,
        MasterRows masterRows,
        Map<String, Optional<HousingLegalDong>> existingLegalDongLookupCache,
        Map<String, PreparedRealEstateImportServiceRequest.ApartmentTradeRawRow> apartmentTradeRawRows,
        Map<String, List<ResolvedTradeCandidate>> candidatesByProviderId
    ) {
        List<ApartmentTradeResponseParser.ApartmentTradeRow> tradeRows =
            parseApartmentTradeResponse(apartmentTradeResponse);

        for (ApartmentTradeResponseParser.ApartmentTradeRow tradeRow : tradeRows) {
            HousingLegalDong legalDong = resolveLegalDong(
                masterRows,
                existingLegalDongLookupCache,
                tradeRow
            );

            PreparedRealEstateImportServiceRequest.ApartmentTradeRawRow tradeRawRow =
                toApartmentTradeRawRow(tradeRow, legalDong);
            apartmentTradeRawRows.putIfAbsent(tradeRawRow.getTradeKey(), tradeRawRow);

            if (legalDong == null) {
                continue;
            }

            String providerId = buildProviderId(legalDong, tradeRow);
            candidatesByProviderId
                .computeIfAbsent(providerId, key -> new ArrayList<>())
                .add(new ResolvedTradeCandidate(legalDong, tradeRow));
        }
    }

    private List<String> resolveApartmentTradeResponses(RealEstateMasterImportRequest request) {
        if (request.getApartmentTradeResponses() == null) {
            return List.of();
        }
        return request.getApartmentTradeResponses();
    }

    private List<ApartmentTradeResponseParser.ApartmentTradeRow> parseApartmentTradeResponse(
        String apartmentTradeResponse
    ) {
        try {
            return apartmentTradeResponseParser.parse(apartmentTradeResponse);
        } catch (RuntimeException exception) {
            log.error(
                "아파트 실거래가 응답 처리 실패 responseLength={}, responsePrefix={}",
                lengthOf(apartmentTradeResponse),
                preview(apartmentTradeResponse, 200),
                exception
            );
            throw exception;
        }
    }

    private MasterRows buildMasterRows(List<LegalDongCodeResponseParser.LegalDongCodeRow> legalDongRows) {
        Map<String, LegalDongCodeResponseParser.LegalDongCodeRow> districtRowsByDistrictCode = new HashMap<>();
        List<LegalDongCodeResponseParser.LegalDongCodeRow> leafRows = new ArrayList<>();

        for (LegalDongCodeResponseParser.LegalDongCodeRow row : legalDongRows) {
            if (row.districtLevel()) {
                districtRowsByDistrictCode.put(row.districtCode(), row);
                continue;
            }
            leafRows.add(row);
        }

        Map<String, HousingRegion> regions = new LinkedHashMap<>();
        Map<String, HousingDistrict> districts = new LinkedHashMap<>();
        Map<String, HousingLegalDong> legalDongs = new LinkedHashMap<>();
        Map<String, HousingLegalDong> legalDongsByDistrictAndName = new HashMap<>();

        for (LegalDongCodeResponseParser.LegalDongCodeRow leafRow : leafRows) {
            String regionName = extractRegionName(leafRow.fullAddressName());
            regions.put(
                leafRow.regionCode(),
                HousingRegion.create(leafRow.regionCode(), regionName)
            );

            LegalDongCodeResponseParser.LegalDongCodeRow districtRow =
                districtRowsByDistrictCode.get(leafRow.districtCode());
            String districtName = resolveDistrictName(leafRow, districtRow);
            String districtLegalDongCode = resolveDistrictLegalDongCode(districtRow);

            districts.put(
                leafRow.districtCode(),
                HousingDistrict.create(
                    leafRow.districtCode(),
                    leafRow.regionCode(),
                    districtName,
                    districtLegalDongCode
                )
            );

            HousingLegalDong legalDong = HousingLegalDong.create(
                leafRow.legalDongCode(),
                leafRow.parentLegalDongCode(),
                leafRow.regionCode(),
                leafRow.districtCode(),
                leafRow.legalDongName(),
                leafRow.fullAddressName()
            );
            legalDongs.put(leafRow.legalDongCode(), legalDong);
            legalDongsByDistrictAndName.put(
                buildLegalDongLookupKey(leafRow.districtCode(), leafRow.legalDongName()),
                legalDong
            );
        }

        return new MasterRows(
            toHousingRegionRows(regions),
            toHousingDistrictRows(districts),
            toHousingLegalDongRows(legalDongs),
            legalDongsByDistrictAndName
        );
    }

    private String resolveDistrictName(
        LegalDongCodeResponseParser.LegalDongCodeRow leafRow,
        LegalDongCodeResponseParser.LegalDongCodeRow districtRow
    ) {
        if (districtRow == null) {
            return extractDistrictName(leafRow.fullAddressName());
        }
        return districtRow.legalDongName();
    }

    private String resolveDistrictLegalDongCode(
        LegalDongCodeResponseParser.LegalDongCodeRow districtRow
    ) {
        if (districtRow == null) {
            return null;
        }
        return districtRow.legalDongCode();
    }

    private List<PreparedRealEstateImportServiceRequest.HousingRegionRow> toHousingRegionRows(
        Map<String, HousingRegion> regions
    ) {
        return regions.values().stream()
            .map(region -> PreparedRealEstateImportServiceRequest.HousingRegionRow.of(
                region.getRegionCode(),
                region.getRegionName()
            ))
            .toList();
    }

    private List<PreparedRealEstateImportServiceRequest.HousingDistrictRow> toHousingDistrictRows(
        Map<String, HousingDistrict> districts
    ) {
        return districts.values().stream()
            .map(district -> PreparedRealEstateImportServiceRequest.HousingDistrictRow.of(
                district.getDistrictCode(),
                district.getRegionCode(),
                district.getDistrictName(),
                district.getDistrictLegalDongCode()
            ))
            .toList();
    }

    private List<PreparedRealEstateImportServiceRequest.HousingLegalDongRow> toHousingLegalDongRows(
        Map<String, HousingLegalDong> legalDongs
    ) {
        return legalDongs.values().stream()
            .map(legalDong -> PreparedRealEstateImportServiceRequest.HousingLegalDongRow.of(
                legalDong.getLegalDongCode(),
                legalDong.getParentLegalDongCode(),
                legalDong.getRegionCode(),
                legalDong.getDistrictCode(),
                legalDong.getLegalDongName(),
                legalDong.getFullAddressName()
            ))
            .toList();
    }

    private HousingLegalDong resolveLegalDong(
        MasterRows masterRows,
        Map<String, Optional<HousingLegalDong>> existingLegalDongLookupCache,
        ApartmentTradeResponseParser.ApartmentTradeRow tradeRow
    ) {
        String lookupKey = buildLegalDongLookupKey(
            tradeRow.districtCode(),
            tradeRow.legalDongName()
        );

        HousingLegalDong preparedLegalDong = masterRows.legalDongsByDistrictAndName().get(lookupKey);
        if (preparedLegalDong != null) {
            return preparedLegalDong;
        }

        return existingLegalDongLookupCache.computeIfAbsent(
            lookupKey,
            key -> housingLegalDongRepository.findByDistrictCodeAndLegalDongName(
                tradeRow.districtCode(),
                tradeRow.legalDongName()
            )
        ).orElse(null);
    }

    private List<PreparedRealEstateImportServiceRequest.RealEstatePropertyRow> buildRepresentativeProperties(
        Map<String, List<ResolvedTradeCandidate>> candidatesByProviderId,
        GeocodingPreparationContext geocodingContext
    ) {
        List<PreparedRealEstateImportServiceRequest.RealEstatePropertyRow> realEstatePropertyRows =
            new ArrayList<>();

        for (Map.Entry<String, List<ResolvedTradeCandidate>> entry : candidatesByProviderId.entrySet()) {
            ResolvedTradeCandidate representative = entry.getValue().stream()
                .max(Comparator
                    .comparing((ResolvedTradeCandidate candidate) -> candidate.tradeRow().dealDate())
                    .thenComparing(candidate -> candidate.tradeRow().dealAmount().getAmount())
                    .thenComparing(candidate -> candidate.tradeRow().exclusiveArea()))
                .orElseThrow();

            String address = buildRepresentativeAddress(representative);
            GeocodingResolution geocodingResolution = resolveGeocoding(
                entry.getKey(),
                representative,
                address,
                geocodingContext
            );

            realEstatePropertyRows.add(
                PreparedRealEstateImportServiceRequest.RealEstatePropertyRow.of(
                    entry.getKey(),
                    representative.tradeRow().apartmentName(),
                    address,
                    representative.legalDong().getRegionCode(),
                    representative.legalDong().getDistrictCode(),
                    representative.legalDong().getLegalDongCode(),
                    representative.tradeRow().dealAmount(),
                    geocodingResolution.latitude(),
                    geocodingResolution.longitude(),
                    PropertyType.APARTMENT,
                    TransactionType.SALE,
                    HousingType.OWNED_APT,
                    List.of()
                )
            );
        }

        return realEstatePropertyRows;
    }

    private PreparedRealEstateImportServiceRequest.ApartmentTradeRawRow toApartmentTradeRawRow(
        ApartmentTradeResponseParser.ApartmentTradeRow tradeRow,
        HousingLegalDong legalDong
    ) {
        return PreparedRealEstateImportServiceRequest.ApartmentTradeRawRow.of(
            buildTradeKey(tradeRow),
            tradeRow.districtCode(),
            tradeRow.legalDongName(),
            resolveLegalDongCode(legalDong),
            tradeRow.apartmentName(),
            tradeRow.jibun(),
            tradeRow.dealDate(),
            tradeRow.dealAmount(),
            tradeRow.exclusiveArea(),
            tradeRow.floor(),
            tradeRow.buildYear(),
            tradeRow.landLeasehold()
        );
    }

    private String resolveLegalDongCode(HousingLegalDong legalDong) {
        if (legalDong == null) {
            return null;
        }
        return legalDong.getLegalDongCode();
    }

    private GeocodingResolution resolveGeocoding(
        String providerId,
        ResolvedTradeCandidate representative,
        String address,
        GeocodingPreparationContext geocodingContext
    ) {
        Optional<GeocodingLookupResult> geocodingResult = geocodeRepresentative(
            providerId,
            representative,
            address,
            geocodingContext
        );
        if (geocodingResult.isEmpty()) {
            return GeocodingResolution.unresolved();
        }

        return GeocodingResolution.of(
            geocodingResult.get().latitude(),
            geocodingResult.get().longitude()
        );
    }

    private Optional<GeocodingLookupResult> geocodeRepresentative(
        String providerId,
        ResolvedTradeCandidate representative,
        String address,
        GeocodingPreparationContext geocodingContext
    ) {
        List<String> queries = buildGeocodingQueries(representative);
        RuntimeException lastException = null;

        for (String query : queries) {
            try {
                GeocodingLookupResult lookupResult =
                    resolveGeocodingLookup(query, geocodingContext);
                if (!lookupResult.hasCoordinates()) {
                    continue;
                }

                logFallbackQuery(providerId, address, query);
                return Optional.of(lookupResult);
            } catch (RuntimeException exception) {
                log.warn(
                    "대표 매물 지오코딩 호출 실패 providerId={}, query={}",
                    providerId,
                    query,
                    exception
                );
                lastException = exception;
            }
        }

        if (lastException == null) {
            log.warn(
                "대표 매물 지오코딩 결과 없음 providerId={}, address={}, legalDongCode={}, queries={}",
                providerId,
                address,
                representative.legalDong().getLegalDongCode(),
                queries
            );
            return Optional.empty();
        }

        log.warn(
            "대표 매물 지오코딩 실패 providerId={}, address={}, legalDongCode={}, queries={}",
            providerId,
            address,
            representative.legalDong().getLegalDongCode(),
            queries,
            lastException
        );
        return Optional.empty();
    }

    private void logFallbackQuery(String providerId, String address, String query) {
        if (query.equals(address)) {
            return;
        }

        log.info(
            "대표 매물 지오코딩 fallback 성공 providerId={}, primaryQuery={}, resolvedQuery={}",
            providerId,
            address,
            query
        );
    }

    private GeocodingLookupResult resolveGeocodingLookup(
        String query,
        GeocodingPreparationContext geocodingContext
    ) {
        GeocodingLookupResult cachedLookup = geocodingContext.lookupResultsByQuery().get(query);
        if (cachedLookup != null) {
            return cachedLookup;
        }

        Optional<RealEstateGeocodeCache> existingCache =
            realEstateGeocodeCacheRepository.findByGeocodingQuery(query);
        if (existingCache.isPresent()) {
            GeocodingLookupResult lookupResult = GeocodingLookupResult.from(existingCache.get());
            geocodingContext.lookupResultsByQuery().put(query, lookupResult);
            return lookupResult;
        }

        Optional<NaverGeocodingClient.GeocodingResult> geocodingResult =
            naverGeocodingClient.geocode(query);

        GeocodingLookupResult lookupResult = geocodingResult
            .map(GeocodingLookupResult::success)
            .orElseGet(GeocodingLookupResult::noResult);

        geocodingContext.lookupResultsByQuery().put(query, lookupResult);
        geocodingContext.pendingCacheRowsByQuery().put(
            query,
            lookupResult.toCacheRow(query)
        );
        return lookupResult;
    }

    private List<String> buildGeocodingQueries(ResolvedTradeCandidate representative) {
        LinkedHashSet<String> queries = new LinkedHashSet<>();
        String fullAddressName = representative.legalDong().getFullAddressName();
        String apartmentName = representative.tradeRow().apartmentName();
        String jibun = representative.tradeRow().jibun();

        addQuery(queries, fullAddressName + " " + jibun);
        addQuery(queries, fullAddressName + " " + apartmentName);
        addQuery(queries, apartmentName + " " + fullAddressName);

        return new ArrayList<>(queries);
    }

    private void addQuery(LinkedHashSet<String> queries, String query) {
        String normalized = normalizeWhitespace(query);
        if (normalized.isBlank()) {
            return;
        }
        queries.add(normalized);
    }

    private String buildRepresentativeAddress(ResolvedTradeCandidate representative) {
        return normalizeWhitespace(
            representative.legalDong().getFullAddressName()
                + " "
                + representative.tradeRow().jibun()
        );
    }

    private String buildTradeKey(ApartmentTradeResponseParser.ApartmentTradeRow tradeRow) {
        return String.join(
            "|",
            tradeRow.districtCode(),
            tradeRow.legalDongName(),
            tradeRow.apartmentName(),
            tradeRow.jibun(),
            tradeRow.dealDate().toString(),
            tradeRow.exclusiveArea().toPlainString(),
            String.valueOf(tradeRow.floor()),
            tradeRow.dealAmount().toString()
        );
    }

    private String buildProviderId(
        HousingLegalDong legalDong,
        ApartmentTradeResponseParser.ApartmentTradeRow tradeRow
    ) {
        return String.join(
            "|",
            "APT",
            legalDong.getLegalDongCode(),
            tradeRow.apartmentName(),
            tradeRow.jibun()
        );
    }

    private String buildLegalDongLookupKey(String districtCode, String legalDongName) {
        return districtCode + "|" + legalDongName;
    }

    private String extractRegionName(String fullAddressName) {
        String[] tokens = splitAddressTokens(fullAddressName);
        if (tokens.length == 0) {
            return "";
        }
        return tokens[0];
    }

    private String extractDistrictName(String fullAddressName) {
        String[] tokens = splitAddressTokens(fullAddressName);
        if (tokens.length < 2) {
            return "";
        }
        return tokens[1];
    }

    private String[] splitAddressTokens(String fullAddressName) {
        if (fullAddressName == null || fullAddressName.isBlank()) {
            return new String[0];
        }
        return fullAddressName.trim().split("\\s+");
    }

    private int lengthOf(String value) {
        if (value == null) {
            return 0;
        }
        return value.length();
    }

    private String normalizeWhitespace(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    private String preview(String value, int limit) {
        if (value == null) {
            return "null";
        }

        String escaped = value
            .replace("\r", "\\r")
            .replace("\n", "\\n")
            .replace("\t", "\\t");

        if (escaped.length() <= limit) {
            return escaped;
        }
        return escaped.substring(0, limit) + "...";
    }

    private static final class ResolvedTradeCandidate {

        private final HousingLegalDong legalDong;
        private final ApartmentTradeResponseParser.ApartmentTradeRow tradeRow;

        private ResolvedTradeCandidate(
            HousingLegalDong legalDong,
            ApartmentTradeResponseParser.ApartmentTradeRow tradeRow
        ) {
            this.legalDong = legalDong;
            this.tradeRow = tradeRow;
        }

        private HousingLegalDong legalDong() {
            return legalDong;
        }

        private ApartmentTradeResponseParser.ApartmentTradeRow tradeRow() {
            return tradeRow;
        }
    }

    private static final class GeocodingResolution {

        private final BigDecimal latitude;
        private final BigDecimal longitude;

        private GeocodingResolution(BigDecimal latitude, BigDecimal longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        private static GeocodingResolution of(
            BigDecimal latitude,
            BigDecimal longitude
        ) {
            return new GeocodingResolution(latitude, longitude);
        }

        private static GeocodingResolution unresolved() {
            return new GeocodingResolution(null, null);
        }

        private BigDecimal latitude() {
            return latitude;
        }

        private BigDecimal longitude() {
            return longitude;
        }
    }

    private static final class MasterRows {

        private final List<PreparedRealEstateImportServiceRequest.HousingRegionRow> housingRegions;
        private final List<PreparedRealEstateImportServiceRequest.HousingDistrictRow> housingDistricts;
        private final List<PreparedRealEstateImportServiceRequest.HousingLegalDongRow> housingLegalDongs;
        private final Map<String, HousingLegalDong> legalDongsByDistrictAndName;

        private MasterRows(
            List<PreparedRealEstateImportServiceRequest.HousingRegionRow> housingRegions,
            List<PreparedRealEstateImportServiceRequest.HousingDistrictRow> housingDistricts,
            List<PreparedRealEstateImportServiceRequest.HousingLegalDongRow> housingLegalDongs,
            Map<String, HousingLegalDong> legalDongsByDistrictAndName
        ) {
            this.housingRegions = housingRegions;
            this.housingDistricts = housingDistricts;
            this.housingLegalDongs = housingLegalDongs;
            this.legalDongsByDistrictAndName = legalDongsByDistrictAndName;
        }

        private List<PreparedRealEstateImportServiceRequest.HousingRegionRow> housingRegions() {
            return housingRegions;
        }

        private List<PreparedRealEstateImportServiceRequest.HousingDistrictRow> housingDistricts() {
            return housingDistricts;
        }

        private List<PreparedRealEstateImportServiceRequest.HousingLegalDongRow> housingLegalDongs() {
            return housingLegalDongs;
        }

        private Map<String, HousingLegalDong> legalDongsByDistrictAndName() {
            return legalDongsByDistrictAndName;
        }
    }

    private static final class GeocodingPreparationContext {

        private final Map<String, GeocodingLookupResult> lookupResultsByQuery;
        private final Map<String, PreparedRealEstateImportServiceRequest.RealEstateGeocodeCacheRow>
            pendingCacheRowsByQuery;

        private GeocodingPreparationContext() {
            this.lookupResultsByQuery = new HashMap<>();
            this.pendingCacheRowsByQuery = new LinkedHashMap<>();
        }

        private Map<String, GeocodingLookupResult> lookupResultsByQuery() {
            return lookupResultsByQuery;
        }

        private Map<String, PreparedRealEstateImportServiceRequest.RealEstateGeocodeCacheRow>
        pendingCacheRowsByQuery() {
            return pendingCacheRowsByQuery;
        }

        private List<PreparedRealEstateImportServiceRequest.RealEstateGeocodeCacheRow> pendingCacheRows() {
            return new ArrayList<>(pendingCacheRowsByQuery.values());
        }
    }

    private static final class GeocodingLookupResult {

        private final GeocodingStatus geocodingStatus;
        private final BigDecimal latitude;
        private final BigDecimal longitude;
        private final String resolvedRoadAddress;
        private final String resolvedJibunAddress;

        private GeocodingLookupResult(
            GeocodingStatus geocodingStatus,
            BigDecimal latitude,
            BigDecimal longitude,
            String resolvedRoadAddress,
            String resolvedJibunAddress
        ) {
            this.geocodingStatus = geocodingStatus;
            this.latitude = latitude;
            this.longitude = longitude;
            this.resolvedRoadAddress = resolvedRoadAddress;
            this.resolvedJibunAddress = resolvedJibunAddress;
        }

        private static GeocodingLookupResult from(RealEstateGeocodeCache cache) {
            return new GeocodingLookupResult(
                cache.getGeocodingStatus(),
                cache.getLatitude(),
                cache.getLongitude(),
                cache.getResolvedRoadAddress(),
                cache.getResolvedJibunAddress()
            );
        }

        private static GeocodingLookupResult success(
            NaverGeocodingClient.GeocodingResult geocodingResult
        ) {
            return new GeocodingLookupResult(
                GeocodingStatus.SUCCESS,
                geocodingResult.latitude(),
                geocodingResult.longitude(),
                geocodingResult.roadAddress(),
                geocodingResult.jibunAddress()
            );
        }

        private static GeocodingLookupResult noResult() {
            return new GeocodingLookupResult(
                GeocodingStatus.NO_RESULT,
                null,
                null,
                null,
                null
            );
        }

        private boolean hasCoordinates() {
            return geocodingStatus == GeocodingStatus.SUCCESS
                && latitude != null
                && longitude != null;
        }

        private BigDecimal latitude() {
            return latitude;
        }

        private BigDecimal longitude() {
            return longitude;
        }

        private PreparedRealEstateImportServiceRequest.RealEstateGeocodeCacheRow toCacheRow(String query) {
            return PreparedRealEstateImportServiceRequest.RealEstateGeocodeCacheRow.of(
                query,
                geocodingStatus,
                latitude,
                longitude,
                resolvedRoadAddress,
                resolvedJibunAddress
            );
        }
    }
}
