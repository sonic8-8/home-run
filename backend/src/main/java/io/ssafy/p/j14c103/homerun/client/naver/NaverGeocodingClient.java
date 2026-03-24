package io.ssafy.p.j14c103.homerun.client.naver;

import io.ssafy.p.j14c103.homerun.config.ConditionalOnRealEstateImportEnabled;
import io.ssafy.p.j14c103.homerun.config.NaverGeocodingProperties;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@ConditionalOnRealEstateImportEnabled
@Slf4j
@RequiredArgsConstructor
public class NaverGeocodingClient {

    private static final String STATUS_OK = "OK";
    private static final String HEADER_CLIENT_ID = "x-ncp-apigw-api-key-id";
    private static final String HEADER_CLIENT_SECRET = "x-ncp-apigw-api-key";
    private static final String QUERY_PARAM = "query";

    private final RestTemplate restTemplate;
    private final NaverGeocodingProperties properties;

    @SuppressWarnings("unchecked")
    public Optional<GeocodingResult> geocode(String query) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HEADER_CLIENT_ID, properties.getClientId());
        headers.set(HEADER_CLIENT_SECRET, properties.getClientSecret());
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        URI uri = UriComponentsBuilder
            .fromHttpUrl(properties.getBaseUrl())
            .queryParam(QUERY_PARAM, query)
            .encode(StandardCharsets.UTF_8)
            .build()
            .toUri();

        ResponseEntity<Map> response = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            new HttpEntity<>(null, headers),
            Map.class
        );

        log.debug(
            "네이버 지오코딩 응답 수신 query={}, statusCode={}, bodyKeys={}",
            query,
            response.getStatusCode().value(),
            response.getBody() != null ? response.getBody().keySet() : List.of()
        );

        Map<String, Object> body = response.getBody();
        if (body == null) {
            log.warn("네이버 지오코딩 응답 본문이 없습니다. query={}", query);
            throw new HomerunException(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID);
        }

        String status = stringValue(body.get("status"));
        if (!STATUS_OK.equalsIgnoreCase(status)) {
            log.warn(
                "네이버 지오코딩 상태가 올바르지 않습니다. query={}, status={}, bodyPreview={}",
                query,
                status,
                preview(body)
            );
            throw new HomerunException(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID);
        }

        int totalCount = extractTotalCount(body.get("meta"));
        if (totalCount <= 0) {
            log.debug(
                "네이버 지오코딩 결과가 없습니다. query={}, bodyPreview={}",
                query,
                preview(body)
            );
            return Optional.empty();
        }

        Object addresses = body.get("addresses");
        if (!(addresses instanceof List<?> addressList) || addressList.isEmpty()) {
            log.warn(
                "네이버 지오코딩 주소 목록 형식이 올바르지 않습니다. query={}, bodyPreview={}",
                query,
                preview(body)
            );
            throw new HomerunException(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID);
        }

        Object firstAddress = addressList.get(0);
        if (!(firstAddress instanceof Map<?, ?> addressMap)) {
            log.warn(
                "네이버 지오코딩 주소 형식이 올바르지 않습니다. query={}, firstAddressType={}",
                query,
                firstAddress != null ? firstAddress.getClass().getName() : "null"
            );
            throw new HomerunException(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID);
        }

        Object latitude = addressMap.get("y");
        Object longitude = addressMap.get("x");
        if (latitude == null || longitude == null) {
            log.warn(
                "네이버 지오코딩 좌표가 없습니다. query={}, addressPreview={}",
                query,
                preview(addressMap)
            );
            throw new HomerunException(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID);
        }

        return Optional.of(
            GeocodingResult.of(
                new BigDecimal(latitude.toString()),
                new BigDecimal(longitude.toString()),
                stringValue(addressMap.get("roadAddress")),
                stringValue(addressMap.get("jibunAddress"))
            )
        );
    }

    @SuppressWarnings("unchecked")
    private int extractTotalCount(Object meta) {
        if (!(meta instanceof Map<?, ?> metaMap)) {
            return 0;
        }

        Object totalCount = ((Map<String, Object>) metaMap).get("totalCount");
        if (totalCount == null) {
            return 0;
        }

        try {
            return Integer.parseInt(totalCount.toString());
        } catch (NumberFormatException exception) {
            log.warn("네이버 지오코딩 totalCount 형식이 올바르지 않습니다. meta={}", preview(metaMap));
            throw new HomerunException(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID, exception);
        }
    }

    private String stringValue(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    private String preview(Object value) {
        if (value == null) {
            return "null";
        }

        String normalized = value.toString()
            .replace("\r", "\\r")
            .replace("\n", "\\n")
            .replace("\t", "\\t");

        if (normalized.length() <= 200) {
            return normalized;
        }
        return normalized.substring(0, 200) + "...";
    }

    public record GeocodingResult(
        BigDecimal latitude,
        BigDecimal longitude,
        String roadAddress,
        String jibunAddress
    ) {

        public static GeocodingResult of(
            BigDecimal latitude,
            BigDecimal longitude,
            String roadAddress,
            String jibunAddress
        ) {
            return new GeocodingResult(latitude, longitude, roadAddress, jibunAddress);
        }
    }
}
