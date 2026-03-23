package io.ssafy.p.j14c103.homerun.client.publicdata.realestate;

import io.ssafy.p.j14c103.homerun.config.PublicDataApiProperties;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApartmentTradeClient {

    private static final String APARTMENT_TRADE_PATH = "/getRTMSDataSvcAptTrade";
    private static final String SERVICE_KEY_PARAM = "serviceKey";
    private static final String DISTRICT_CODE_PARAM = "LAWD_CD";
    private static final String DEAL_YEAR_MONTH_PARAM = "DEAL_YMD";

    private final RestTemplate restTemplate;
    private final PublicDataApiProperties properties;

    public String fetch(String districtCode, YearMonth dealYearMonth, int pageNo, int numOfRows) {
        URI uri = UriComponentsBuilder
            .fromHttpUrl(properties.getApartmentTradeBaseUrl())
            .path(APARTMENT_TRADE_PATH)
            .queryParam(SERVICE_KEY_PARAM, properties.getServiceKey())
            .queryParam(DISTRICT_CODE_PARAM, districtCode)
            .queryParam(DEAL_YEAR_MONTH_PARAM, formatYearMonth(dealYearMonth))
            .queryParam("pageNo", pageNo)
            .queryParam("numOfRows", numOfRows)
            .encode(StandardCharsets.UTF_8)
            .build()
            .toUri();

        String response = restTemplate.getForObject(uri, String.class);
        if (response == null) {
            throw new HomerunException(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID);
        }

        log.debug(
            "아파트 실거래가 응답 수신 districtCode={}, dealYearMonth={}, pageNo={}, numOfRows={}, responseLength={}",
            districtCode,
            formatYearMonth(dealYearMonth),
            pageNo,
            numOfRows,
            response.length()
        );
        warnIfUnexpectedResponse(
            response,
            "<response",
            "<RESULT",
            "<?xml",
            "{"
        );

        return response;
    }

    private String formatYearMonth(YearMonth dealYearMonth) {
        return String.format("%04d%02d", dealYearMonth.getYear(), dealYearMonth.getMonthValue());
    }

    private void warnIfUnexpectedResponse(String response, String... expectedRoots) {
        String normalized = normalizeXmlPrefix(response);
        if (response.isBlank() || !looksLikeXml(normalized, expectedRoots)) {
            log.warn(
                "아파트 실거래가 응답 형식이 예상과 다릅니다. responsePrefix={}, leadingCodePoints={}",
                preview(response, 200),
                leadingCodePoints(response, 8)
            );
        }
    }

    private String normalizeXmlPrefix(String value) {
        if (value == null) {
            return "";
        }

        String normalized = value.stripLeading();
        if (!normalized.isEmpty() && normalized.charAt(0) == '\uFEFF') {
            normalized = normalized.substring(1).stripLeading();
        }

        int firstAngleBracket = normalized.indexOf('<');
        if (firstAngleBracket > 0) {
            normalized = normalized.substring(firstAngleBracket);
        }

        return normalized;
    }

    private boolean looksLikeXml(String normalized, String... expectedRoots) {
        for (String expectedRoot : expectedRoots) {
            if (normalized.startsWith(expectedRoot)) {
                return true;
            }
        }
        return false;
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

    private String leadingCodePoints(String value, int limit) {
        if (value == null || value.isEmpty()) {
            return "[]";
        }

        StringBuilder builder = new StringBuilder("[");
        int count = 0;
        for (int index = 0; index < value.length() && count < limit; count++) {
            int codePoint = value.codePointAt(index);
            if (count > 0) {
                builder.append(", ");
            }
            builder.append("0x").append(Integer.toHexString(codePoint).toUpperCase());
            index += Character.charCount(codePoint);
        }
        builder.append("]");
        return builder.toString();
    }
}
