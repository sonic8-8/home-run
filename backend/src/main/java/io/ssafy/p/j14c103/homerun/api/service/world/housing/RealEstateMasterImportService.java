package io.ssafy.p.j14c103.homerun.api.service.world.housing;

import io.ssafy.p.j14c103.homerun.api.service.world.housing.request.RealEstateMasterImportRequest;
import io.ssafy.p.j14c103.homerun.client.naver.NaverGeocodingClient;
import io.ssafy.p.j14c103.homerun.client.publicdata.realestate.ApartmentTradeResponseParser;
import io.ssafy.p.j14c103.homerun.client.publicdata.realestate.LegalDongCodeResponseParser;
import io.ssafy.p.j14c103.homerun.config.ConditionalOnRealEstateImportEnabled;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingDistrict;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingDistrictRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.GeocodingStatus;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingLegalDong;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingLegalDongRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingRegion;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingRegionRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateGeocodeCache;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateGeocodeCacheRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.domain.world.housing.PropertyType;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateProperty;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstatePropertyRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.TransactionType;
import io.ssafy.p.j14c103.homerun.domain.world.housing.trade.ApartmentTradeRaw;
import io.ssafy.p.j14c103.homerun.domain.world.housing.trade.ApartmentTradeRawRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnRealEstateImportEnabled
@Slf4j
@Transactional
@RequiredArgsConstructor
public class RealEstateMasterImportService {

    private final LegalDongCodeResponseParser legalDongCodeResponseParser;
    private final ApartmentTradeResponseParser apartmentTradeResponseParser;
    private final HousingRegionRepository housingRegionRepository;
    private final HousingDistrictRepository housingDistrictRepository;
    private final HousingLegalDongRepository housingLegalDongRepository;
    private final ApartmentTradeRawRepository apartmentTradeRawRepository;
    private final RealEstateGeocodeCacheRepository realEstateGeocodeCacheRepository;
    private final RealEstatePropertyRepository realEstatePropertyRepository;
    private final NaverGeocodingClient naverGeocodingClient;

    public void importMaster(RealEstateMasterImportRequest request) {
        List<LegalDongCodeResponseParser.LegalDongCodeRow> legalDongRows =
            legalDongCodeResponseParser.parse(request.getLegalDongXml());

        upsertLegalDongMasters(legalDongRows);

        List<ResolvedTradeCandidate> resolvedTradeCandidates = new ArrayList<>();
        List<String> apartmentTradeResponses = request.getApartmentTradeResponses() != null
            ? request.getApartmentTradeResponses()
            : List.of();

        for (int index = 0; index < apartmentTradeResponses.size(); index++) {
            String apartmentTradeResponse = apartmentTradeResponses.get(index);
            List<ApartmentTradeResponseParser.ApartmentTradeRow> tradeRows =
                parseApartmentTradeResponse(index, apartmentTradeResponse);

            for (ApartmentTradeResponseParser.ApartmentTradeRow tradeRow : tradeRows) {
                Optional<HousingLegalDong> legalDong = housingLegalDongRepository
                    .findByDistrictCodeAndLegalDongName(
                        tradeRow.districtCode(),
                        tradeRow.legalDongName()
                    );

                upsertTradeRaw(tradeRow, legalDong.orElse(null));
                legalDong.ifPresent(value -> resolvedTradeCandidates.add(
                    new ResolvedTradeCandidate(value, tradeRow)
                ));
            }
        }

        upsertRepresentativeProperties(resolvedTradeCandidates);
    }

    private List<ApartmentTradeResponseParser.ApartmentTradeRow> parseApartmentTradeResponse(
        int responseIndex,
        String apartmentTradeResponse
    ) {
        try {
            return apartmentTradeResponseParser.parse(apartmentTradeResponse);
        } catch (RuntimeException exception) {
            log.error(
                "아파트 실거래가 응답 처리 실패 responseIndex={}, responseLength={}, responsePrefix={}",
                responseIndex,
                lengthOf(apartmentTradeResponse),
                preview(apartmentTradeResponse, 200),
                exception
            );
            throw exception;
        }
    }

    private void upsertLegalDongMasters(List<LegalDongCodeResponseParser.LegalDongCodeRow> legalDongRows) {
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

        for (LegalDongCodeResponseParser.LegalDongCodeRow leafRow : leafRows) {
            String regionName = extractRegionName(leafRow.fullAddressName());
            regions.put(
                leafRow.regionCode(),
                HousingRegion.create(leafRow.regionCode(), regionName)
            );

            LegalDongCodeResponseParser.LegalDongCodeRow districtRow =
                districtRowsByDistrictCode.get(leafRow.districtCode());
            String districtName = districtRow != null
                ? districtRow.legalDongName()
                : extractDistrictName(leafRow.fullAddressName());
            String districtLegalDongCode = districtRow != null
                ? districtRow.legalDongCode()
                : null;

            districts.put(
                leafRow.districtCode(),
                HousingDistrict.create(
                    leafRow.districtCode(),
                    leafRow.regionCode(),
                    districtName,
                    districtLegalDongCode
                )
            );

            legalDongs.put(
                leafRow.legalDongCode(),
                HousingLegalDong.create(
                    leafRow.legalDongCode(),
                    leafRow.parentLegalDongCode(),
                    leafRow.regionCode(),
                    leafRow.districtCode(),
                    leafRow.legalDongName(),
                    leafRow.fullAddressName()
                )
            );
        }

        for (HousingRegion region : regions.values()) {
            housingRegionRepository.findById(region.getRegionCode())
                .ifPresentOrElse(
                    existing -> existing.update(region.getRegionName()),
                    () -> housingRegionRepository.save(region)
                );
        }

        for (HousingDistrict district : districts.values()) {
            housingDistrictRepository.findById(district.getDistrictCode())
                .ifPresentOrElse(
                    existing -> existing.update(
                        district.getRegionCode(),
                        district.getDistrictName(),
                        district.getDistrictLegalDongCode()
                    ),
                    () -> housingDistrictRepository.save(district)
                );
        }

        for (HousingLegalDong legalDong : legalDongs.values()) {
            housingLegalDongRepository.findById(legalDong.getLegalDongCode())
                .ifPresentOrElse(
                    existing -> existing.update(
                        legalDong.getParentLegalDongCode(),
                        legalDong.getRegionCode(),
                        legalDong.getDistrictCode(),
                        legalDong.getLegalDongName(),
                        legalDong.getFullAddressName()
                    ),
                    () -> housingLegalDongRepository.save(legalDong)
                );
        }
    }

    private void upsertTradeRaw(
        ApartmentTradeResponseParser.ApartmentTradeRow tradeRow,
        HousingLegalDong legalDong
    ) {
        String legalDongCode = legalDong != null ? legalDong.getLegalDongCode() : null;
        String tradeKey = buildTradeKey(tradeRow);

        if (apartmentTradeRawRepository.findByTradeKey(tradeKey).isPresent()) {
            return;
        }

        apartmentTradeRawRepository.save(
            ApartmentTradeRaw.create(
                tradeKey,
                tradeRow.districtCode(),
                tradeRow.legalDongName(),
                legalDongCode,
                tradeRow.apartmentName(),
                tradeRow.jibun(),
                tradeRow.dealDate(),
                tradeRow.dealAmount(),
                tradeRow.exclusiveArea(),
                tradeRow.floor(),
                tradeRow.buildYear(),
                tradeRow.landLeasehold()
            )
        );
    }

    private void upsertRepresentativeProperties(List<ResolvedTradeCandidate> candidates) {
        Map<String, List<ResolvedTradeCandidate>> candidatesByProviderId = new LinkedHashMap<>();

        for (ResolvedTradeCandidate candidate : candidates) {
            String providerId = buildProviderId(candidate.legalDong(), candidate.tradeRow());
            candidatesByProviderId
                .computeIfAbsent(providerId, key -> new ArrayList<>())
                .add(candidate);
        }

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
                address
            );

            realEstatePropertyRepository.findByProviderId(entry.getKey())
                .ifPresentOrElse(
                    existing -> existing.updateFromImport(
                        representative.tradeRow().apartmentName(),
                        address,
                        representative.legalDong().getRegionCode(),
                        representative.legalDong().getDistrictCode(),
                        representative.legalDong().getLegalDongCode(),
                        representative.tradeRow().dealAmount(),
                        resolveLatitude(existing, geocodingResolution),
                        resolveLongitude(existing, geocodingResolution),
                        PropertyType.APARTMENT,
                        TransactionType.SALE,
                        HousingType.OWNED_APT,
                        List.of()
                    ),
                    () -> realEstatePropertyRepository.save(
                        RealEstateProperty.create(
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
                    )
                );
        }
    }

    private GeocodingResolution resolveGeocoding(
        String providerId,
        ResolvedTradeCandidate representative,
        String address
    ) {
        Optional<NaverGeocodingClient.GeocodingResult> geocodingResult = geocodeRepresentative(
            providerId,
            representative,
            address
        );
        if (geocodingResult.isEmpty()) {
            return GeocodingResolution.unresolved();
        }

        return GeocodingResolution.of(
            geocodingResult.get().latitude(),
            geocodingResult.get().longitude()
        );
    }

    private Optional<NaverGeocodingClient.GeocodingResult> geocodeRepresentative(
        String providerId,
        ResolvedTradeCandidate representative,
        String address
    ) {
        List<String> queries = buildGeocodingQueries(representative);
        RuntimeException lastException = null;

        for (String query : queries) {
            Optional<RealEstateGeocodeCache> cache = realEstateGeocodeCacheRepository
                .findByGeocodingQuery(query);
            if (cache.isPresent()) {
                if (cache.get().getGeocodingStatus() == GeocodingStatus.NO_RESULT) {
                    continue;
                }
                if (!query.equals(address)) {
                    log.info(
                        "대표 매물 지오코딩 캐시 사용 providerId={}, primaryQuery={}, resolvedQuery={}",
                        providerId,
                        address,
                        query
                    );
                }
                return Optional.of(NaverGeocodingClient.GeocodingResult.of(
                    cache.get().getLatitude(),
                    cache.get().getLongitude(),
                    cache.get().getResolvedRoadAddress(),
                    cache.get().getResolvedJibunAddress()
                ));
            }

            try {
                Optional<NaverGeocodingClient.GeocodingResult> result = naverGeocodingClient.geocode(query);
                if (result.isEmpty()) {
                    cacheGeocodingNoResult(query);
                    continue;
                }

                cacheGeocodingSuccess(query, result.get());
                if (!query.equals(address)) {
                    log.info(
                        "대표 매물 지오코딩 fallback 성공 providerId={}, primaryQuery={}, resolvedQuery={}",
                        providerId,
                        address,
                        query
                    );
                }
                return result;
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

    private void cacheGeocodingSuccess(
        String query,
        NaverGeocodingClient.GeocodingResult geocodingResult
    ) {
        realEstateGeocodeCacheRepository.findByGeocodingQuery(query)
            .ifPresentOrElse(
                existing -> existing.updateSuccess(
                    geocodingResult.latitude(),
                    geocodingResult.longitude(),
                    geocodingResult.roadAddress(),
                    geocodingResult.jibunAddress()
                ),
                () -> realEstateGeocodeCacheRepository.save(
                    RealEstateGeocodeCache.success(
                        query,
                        geocodingResult.latitude(),
                        geocodingResult.longitude(),
                        geocodingResult.roadAddress(),
                        geocodingResult.jibunAddress()
                    )
                )
            );
    }

    private void cacheGeocodingNoResult(String query) {
        realEstateGeocodeCacheRepository.findByGeocodingQuery(query)
            .ifPresentOrElse(
                RealEstateGeocodeCache::updateNoResult,
                () -> realEstateGeocodeCacheRepository.save(
                    RealEstateGeocodeCache.noResult(query)
                )
            );
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

    private BigDecimal resolveLatitude(
        RealEstateProperty existing,
        GeocodingResolution geocodingResolution
    ) {
        if (geocodingResolution.hasCoordinates()) {
            return geocodingResolution.latitude();
        }
        return existing.getLatitude();
    }

    private BigDecimal resolveLongitude(
        RealEstateProperty existing,
        GeocodingResolution geocodingResolution
    ) {
        if (geocodingResolution.hasCoordinates()) {
            return geocodingResolution.longitude();
        }
        return existing.getLongitude();
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
        return value == null ? 0 : value.length();
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

    private record ResolvedTradeCandidate(
        HousingLegalDong legalDong,
        ApartmentTradeResponseParser.ApartmentTradeRow tradeRow
    ) {
    }

    private record GeocodingResolution(
        BigDecimal latitude,
        BigDecimal longitude
    ) {

        private static GeocodingResolution of(
            BigDecimal latitude,
            BigDecimal longitude
        ) {
            return new GeocodingResolution(latitude, longitude);
        }

        private static GeocodingResolution unresolved() {
            return new GeocodingResolution(null, null);
        }

        private boolean hasCoordinates() {
            return latitude != null && longitude != null;
        }
    }
}
