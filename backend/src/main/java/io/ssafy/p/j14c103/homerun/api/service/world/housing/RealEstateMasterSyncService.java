package io.ssafy.p.j14c103.homerun.api.service.world.housing;

import io.ssafy.p.j14c103.homerun.api.service.world.housing.request.RealEstateMasterImportServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.request.RealEstateMasterSyncServiceRequest;
import io.ssafy.p.j14c103.homerun.client.publicdata.realestate.ApartmentTradeClient;
import io.ssafy.p.j14c103.homerun.client.publicdata.realestate.LegalDongCodeClient;
import io.ssafy.p.j14c103.homerun.client.publicdata.realestate.LegalDongCodeXmlParser;
import io.ssafy.p.j14c103.homerun.config.PublicDataApiProperties;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RealEstateMasterSyncService {

    private static final Pattern TOTAL_COUNT_PATTERN =
        Pattern.compile("<totalCount>\\s*(\\d+)\\s*</totalCount>");

    private final LegalDongCodeClient legalDongCodeClient;
    private final ApartmentTradeClient apartmentTradeClient;
    private final LegalDongCodeXmlParser legalDongCodeXmlParser;
    private final RealEstateMasterImportService realEstateMasterImportService;
    private final PublicDataApiProperties publicDataApiProperties;

    public void sync(RealEstateMasterSyncServiceRequest request) {
        validateRequest(request);

        for (String regionCode : request.getRegions()) {
            String regionAddress = resolveRegionAddress(regionCode);
            List<String> legalDongXmlPages = fetchLegalDongXmlPages(regionAddress);
            String mergedLegalDongXml = mergeLegalDongXmlPages(legalDongXmlPages);

            List<String> districtCodes = legalDongCodeXmlParser.parse(mergedLegalDongXml).stream()
                .filter(row -> !row.districtLevel())
                .map(LegalDongCodeXmlParser.LegalDongCodeRow::districtCode)
                .distinct()
                .collect(Collectors.toList());

            List<String> apartmentTradeXmlPages = new ArrayList<>();
            for (String districtCode : districtCodes) {
                for (YearMonth cursor = request.getFromYearMonth();
                     !cursor.isAfter(request.getToYearMonth());
                     cursor = cursor.plusMonths(1)) {
                    apartmentTradeXmlPages.addAll(fetchApartmentTradeXmlPages(districtCode, cursor));
                }
            }

            realEstateMasterImportService.importMaster(
                RealEstateMasterImportServiceRequest.of(
                    mergedLegalDongXml,
                    apartmentTradeXmlPages
                )
            );
        }
    }

    private void validateRequest(RealEstateMasterSyncServiceRequest request) {
        if (request.getRegions() == null || request.getRegions().isEmpty()) {
            throw new IllegalArgumentException("적재 대상 지역은 필수입니다.");
        }
        if (request.getFromYearMonth() == null || request.getToYearMonth() == null) {
            throw new IllegalArgumentException("적재 대상 기간은 필수입니다.");
        }
        if (request.getFromYearMonth().isAfter(request.getToYearMonth())) {
            throw new IllegalArgumentException("시작 월은 종료 월보다 늦을 수 없습니다.");
        }
    }

    private String resolveRegionAddress(String regionCode) {
        if ("SEOUL".equals(regionCode)) {
            return "서울특별시";
        }
        if ("GWANGJU".equals(regionCode)) {
            return "광주광역시";
        }
        throw new IllegalArgumentException("MVP 범위를 벗어난 지역 코드입니다.");
    }

    private List<String> fetchLegalDongXmlPages(String regionAddress) {
        List<String> xmlPages = new ArrayList<>();
        int totalPages = 1;

        for (int pageNo = 1; pageNo <= totalPages; pageNo++) {
            String xml = legalDongCodeClient.fetch(
                regionAddress,
                pageNo,
                publicDataApiProperties.getPageSize()
            );
            xmlPages.add(xml);

            if (pageNo == 1) {
                totalPages = calculateTotalPages(xml);
            }
        }

        return xmlPages;
    }

    private List<String> fetchApartmentTradeXmlPages(String districtCode, YearMonth yearMonth) {
        List<String> xmlPages = new ArrayList<>();
        int totalPages = 1;

        for (int pageNo = 1; pageNo <= totalPages; pageNo++) {
            String xml = apartmentTradeClient.fetch(
                districtCode,
                yearMonth,
                pageNo,
                publicDataApiProperties.getPageSize()
            );
            xmlPages.add(xml);

            if (pageNo == 1) {
                totalPages = calculateTotalPages(xml);
            }
        }

        return xmlPages;
    }

    private int calculateTotalPages(String xml) {
        Matcher matcher = TOTAL_COUNT_PATTERN.matcher(xml);
        if (!matcher.find()) {
            return 1;
        }

        int totalCount = Integer.parseInt(matcher.group(1));
        if (totalCount <= 0) {
            return 1;
        }

        return (int) Math.ceil(totalCount / (double) publicDataApiProperties.getPageSize());
    }

    private String mergeLegalDongXmlPages(List<String> xmlPages) {
        if (xmlPages.size() == 1) {
            return xmlPages.get(0);
        }

        List<LegalDongCodeXmlParser.LegalDongCodeRow> rows = new ArrayList<>();
        for (String xmlPage : xmlPages) {
            rows.addAll(legalDongCodeXmlParser.parse(xmlPage));
        }

        Map<String, LegalDongCodeXmlParser.LegalDongCodeRow> distinctRows = new LinkedHashMap<>();
        for (LegalDongCodeXmlParser.LegalDongCodeRow row : rows) {
            distinctRows.put(row.legalDongCode(), row);
        }

        StringBuilder builder = new StringBuilder();
        builder.append("<StanReginCd>");
        builder.append("<head><RESULT><resultCode>INFO-0</resultCode><resultMsg>NOMAL SERVICE</resultMsg></RESULT></head>");

        for (LegalDongCodeXmlParser.LegalDongCodeRow row : new LinkedHashSet<>(distinctRows.values())) {
            builder.append("<row>");
            builder.append(tag("region_cd", row.legalDongCode()));
            builder.append(tag("sido_cd", row.regionCode()));
            builder.append(tag("sgg_cd", row.districtCode().substring(2)));
            builder.append(tag("umd_cd", row.districtLevel() ? "000" : row.legalDongCode().substring(5, 8)));
            builder.append(tag("ri_cd", "00"));
            builder.append(tag("locatadd_nm", row.fullAddressName()));
            builder.append(tag("locathigh_cd", row.parentLegalDongCode()));
            builder.append(tag("locallow_nm", row.legalDongName()));
            builder.append("</row>");
        }

        builder.append("</StanReginCd>");
        return builder.toString();
    }

    private String tag(String name, String value) {
        return "<" + name + ">" + escapeXml(value) + "</" + name + ">";
    }

    private String escapeXml(String value) {
        if (value == null) {
            return "";
        }

        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;");
    }
}
