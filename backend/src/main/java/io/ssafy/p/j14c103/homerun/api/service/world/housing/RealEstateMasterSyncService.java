package io.ssafy.p.j14c103.homerun.api.service.world.housing;

import io.ssafy.p.j14c103.homerun.api.service.world.housing.request.RealEstateMasterImportRequest;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.request.RealEstateMasterSyncRequest;
import io.ssafy.p.j14c103.homerun.client.publicdata.realestate.ApartmentTradeClient;
import io.ssafy.p.j14c103.homerun.client.publicdata.realestate.LegalDongCodeClient;
import io.ssafy.p.j14c103.homerun.client.publicdata.realestate.LegalDongCodeResponseParser;
import io.ssafy.p.j14c103.homerun.config.PublicDataApiProperties;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RealEstateMasterSyncService {

    private static final String REGION_CODE_SEOUL = "SEOUL";
    private static final String REGION_CODE_GWANGJU = "GWANGJU";
    private static final String REGION_ADDRESS_SEOUL = "서울특별시";
    private static final String REGION_ADDRESS_GWANGJU = "광주광역시";
    private static final String DISTRICT_LEVEL_UMD_CODE = "000";
    private static final String DEFAULT_RI_CODE = "00";
    private static final String MERGED_RESULT_CODE = "INFO-0";
    private static final String MERGED_RESULT_MESSAGE = "NOMAL SERVICE";
    private static final Pattern TOTAL_COUNT_PATTERN =
        Pattern.compile("<totalCount>\\s*(\\d+)\\s*</totalCount>");
    private static final Pattern JSON_TOTAL_COUNT_PATTERN =
        Pattern.compile("\"totalCount\"\\s*:\\s*\"?(\\d+)\"?");
    private static final Map<String, String> SUPPORTED_REGION_ADDRESSES = Map.of(
        REGION_CODE_SEOUL, REGION_ADDRESS_SEOUL,
        REGION_CODE_GWANGJU, REGION_ADDRESS_GWANGJU
    );

    private final LegalDongCodeClient legalDongCodeClient;
    private final ApartmentTradeClient apartmentTradeClient;
    private final LegalDongCodeResponseParser legalDongCodeResponseParser;
    private final RealEstateMasterImportService realEstateMasterImportService;
    private final PublicDataApiProperties publicDataApiProperties;

    public void sync(RealEstateMasterSyncRequest request) {
        validateRequest(request);

        for (String regionCode : request.getRegions()) {
            String regionAddress = resolveRegionAddress(regionCode);
            List<String> legalDongXmlPages = fetchLegalDongXmlPages(regionAddress);
            String mergedLegalDongXml = mergeLegalDongXmlPages(legalDongXmlPages);
            List<LegalDongCodeResponseParser.LegalDongCodeRow> legalDongRows =
                legalDongCodeResponseParser.parse(mergedLegalDongXml);

            if (legalDongRows.isEmpty()) {
                throw new HomerunException(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID);
            }

            List<String> districtCodes = legalDongRows.stream()
                .filter(row -> !row.districtLevel())
                .map(LegalDongCodeResponseParser.LegalDongCodeRow::districtCode)
                .distinct()
                .collect(Collectors.toList());

            List<String> apartmentTradeResponses = new ArrayList<>();
            for (String districtCode : districtCodes) {
                for (YearMonth cursor = request.getFromYearMonth();
                     !cursor.isAfter(request.getToYearMonth());
                     cursor = cursor.plusMonths(1)) {
                    apartmentTradeResponses.addAll(fetchApartmentTradeResponses(districtCode, cursor));
                }
            }

            log.info(
                "부동산 마스터 동기화 준비 regionCode={}, regionAddress={}, legalDongPages={}, districtCount={}, apartmentTradePageCount={}",
                regionCode,
                regionAddress,
                legalDongXmlPages.size(),
                districtCodes.size(),
                apartmentTradeResponses.size()
            );

            realEstateMasterImportService.importMaster(
                RealEstateMasterImportRequest.of(
                    mergedLegalDongXml,
                    apartmentTradeResponses
                )
            );
        }
    }

    private void validateRequest(RealEstateMasterSyncRequest request) {
        if (request.getRegions() == null || request.getRegions().isEmpty()) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (request.getFromYearMonth() == null || request.getToYearMonth() == null) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (request.getFromYearMonth().isAfter(request.getToYearMonth())) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private String resolveRegionAddress(String regionCode) {
        String regionAddress = SUPPORTED_REGION_ADDRESSES.get(regionCode);
        if (regionAddress == null) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return regionAddress;
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

    private List<String> fetchApartmentTradeResponses(String districtCode, YearMonth yearMonth) {
        List<String> responses = new ArrayList<>();
        int totalPages = 1;

        for (int pageNo = 1; pageNo <= totalPages; pageNo++) {
            String response = apartmentTradeClient.fetch(
                districtCode,
                yearMonth,
                pageNo,
                publicDataApiProperties.getPageSize()
            );
            responses.add(response);

            if (pageNo == 1) {
                totalPages = calculateTotalPages(response);
            }
        }

        return responses;
    }

    private int calculateTotalPages(String xml) {
        Matcher matcher = TOTAL_COUNT_PATTERN.matcher(xml);
        if (!matcher.find()) {
            matcher = JSON_TOTAL_COUNT_PATTERN.matcher(xml);
            if (!matcher.find()) {
                return 1;
            }
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

        List<LegalDongCodeResponseParser.LegalDongCodeRow> rows = new ArrayList<>();
        for (String xmlPage : xmlPages) {
            rows.addAll(legalDongCodeResponseParser.parse(xmlPage));
        }

        Map<String, LegalDongCodeResponseParser.LegalDongCodeRow> distinctRows = new LinkedHashMap<>();
        for (LegalDongCodeResponseParser.LegalDongCodeRow row : rows) {
            distinctRows.put(row.legalDongCode(), row);
        }

        StringBuilder builder = new StringBuilder();
        builder.append("<StanReginCd>");
        builder.append("<head><RESULT><resultCode>")
            .append(MERGED_RESULT_CODE)
            .append("</resultCode><resultMsg>")
            .append(MERGED_RESULT_MESSAGE)
            .append("</resultMsg></RESULT></head>");

        for (LegalDongCodeResponseParser.LegalDongCodeRow row : new LinkedHashSet<>(distinctRows.values())) {
            builder.append("<row>");
            builder.append(tag("region_cd", row.legalDongCode()));
            builder.append(tag("sido_cd", row.regionCode()));
            builder.append(tag("sgg_cd", row.districtCode().substring(2)));
            builder.append(tag(
                "umd_cd",
                row.districtLevel() ? DISTRICT_LEVEL_UMD_CODE : row.legalDongCode().substring(5, 8)
            ));
            builder.append(tag("ri_cd", DEFAULT_RI_CODE));
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
