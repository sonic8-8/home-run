package io.ssafy.p.j14c103.homerun.api.service.world.housing;

import io.ssafy.p.j14c103.homerun.api.service.world.housing.request.RealEstateMasterImportServiceRequest;
import io.ssafy.p.j14c103.homerun.client.naver.NaverGeocodingClient;
import io.ssafy.p.j14c103.homerun.client.publicdata.realestate.ApartmentTradeXmlParser;
import io.ssafy.p.j14c103.homerun.client.publicdata.realestate.LegalDongCodeXmlParser;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingDistrict;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingDistrictRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingLegalDong;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingLegalDongRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingRegion;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingRegionRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.domain.world.housing.PropertyType;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateProperty;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstatePropertyRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.TransactionType;
import io.ssafy.p.j14c103.homerun.domain.world.housing.trade.ApartmentTradeRaw;
import io.ssafy.p.j14c103.homerun.domain.world.housing.trade.ApartmentTradeRawRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RealEstateMasterImportService {

    private final LegalDongCodeXmlParser legalDongCodeXmlParser;
    private final ApartmentTradeXmlParser apartmentTradeXmlParser;
    private final HousingRegionRepository housingRegionRepository;
    private final HousingDistrictRepository housingDistrictRepository;
    private final HousingLegalDongRepository housingLegalDongRepository;
    private final ApartmentTradeRawRepository apartmentTradeRawRepository;
    private final RealEstatePropertyRepository realEstatePropertyRepository;
    private final NaverGeocodingClient naverGeocodingClient;

    public void importMaster(RealEstateMasterImportServiceRequest request) {
        List<LegalDongCodeXmlParser.LegalDongCodeRow> legalDongRows =
            legalDongCodeXmlParser.parse(request.getLegalDongXml());

        upsertLegalDongMasters(legalDongRows);

        List<ResolvedTradeCandidate> resolvedTradeCandidates = new ArrayList<>();
        List<String> apartmentTradeXmls = request.getApartmentTradeXmls() != null
            ? request.getApartmentTradeXmls()
            : List.of();

        for (String apartmentTradeXml : apartmentTradeXmls) {
            List<ApartmentTradeXmlParser.ApartmentTradeRow> tradeRows =
                apartmentTradeXmlParser.parse(apartmentTradeXml);

            for (ApartmentTradeXmlParser.ApartmentTradeRow tradeRow : tradeRows) {
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

    private void upsertLegalDongMasters(List<LegalDongCodeXmlParser.LegalDongCodeRow> legalDongRows) {
        Map<String, LegalDongCodeXmlParser.LegalDongCodeRow> districtRowsByDistrictCode = new HashMap<>();
        List<LegalDongCodeXmlParser.LegalDongCodeRow> leafRows = new ArrayList<>();

        for (LegalDongCodeXmlParser.LegalDongCodeRow row : legalDongRows) {
            if (row.districtLevel()) {
                districtRowsByDistrictCode.put(row.districtCode(), row);
                continue;
            }
            leafRows.add(row);
        }

        Map<String, HousingRegion> regions = new LinkedHashMap<>();
        Map<String, HousingDistrict> districts = new LinkedHashMap<>();
        Map<String, HousingLegalDong> legalDongs = new LinkedHashMap<>();

        for (LegalDongCodeXmlParser.LegalDongCodeRow leafRow : leafRows) {
            String regionName = extractRegionName(leafRow.fullAddressName());
            regions.put(
                leafRow.regionCode(),
                HousingRegion.create(leafRow.regionCode(), regionName)
            );

            LegalDongCodeXmlParser.LegalDongCodeRow districtRow =
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
        ApartmentTradeXmlParser.ApartmentTradeRow tradeRow,
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

            String address = representative.legalDong().getFullAddressName()
                + " "
                + representative.tradeRow().jibun();

            NaverGeocodingClient.GeocodingResult geocodingResult;
            try {
                geocodingResult = naverGeocodingClient.geocode(address);
            } catch (RuntimeException exception) {
                // geocoding 실패는 raw 보존 후 대표 매물 생성만 생략한다.
                continue;
            }

            realEstatePropertyRepository.findByProviderId(entry.getKey())
                .ifPresentOrElse(
                    existing -> existing.updateFromImport(
                        representative.tradeRow().apartmentName(),
                        address,
                        representative.legalDong().getRegionCode(),
                        representative.legalDong().getDistrictCode(),
                        representative.legalDong().getLegalDongCode(),
                        representative.tradeRow().dealAmount(),
                        geocodingResult.latitude(),
                        geocodingResult.longitude(),
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
                            geocodingResult.latitude(),
                            geocodingResult.longitude(),
                            PropertyType.APARTMENT,
                            TransactionType.SALE,
                            HousingType.OWNED_APT,
                            List.of()
                        )
                    )
                );
        }
    }

    private String buildTradeKey(ApartmentTradeXmlParser.ApartmentTradeRow tradeRow) {
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
        ApartmentTradeXmlParser.ApartmentTradeRow tradeRow
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

    private record ResolvedTradeCandidate(
        HousingLegalDong legalDong,
        ApartmentTradeXmlParser.ApartmentTradeRow tradeRow
    ) {
    }
}
