package io.ssafy.p.j14c103.homerun.client.naver;

import io.ssafy.p.j14c103.homerun.config.NaverGeocodingProperties;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class NaverGeocodingClient {

    private final RestTemplate restTemplate;
    private final NaverGeocodingProperties properties;

    @SuppressWarnings("unchecked")
    public GeocodingResult geocode(String query) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-ncp-apigw-api-key-id", properties.getClientId());
        headers.set("x-ncp-apigw-api-key", properties.getClientSecret());

        String url = UriComponentsBuilder
            .fromHttpUrl(properties.getBaseUrl())
            .queryParam("query", query)
            .toUriString();

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(null, headers),
            Map.class
        );

        Map<String, Object> body = response.getBody();
        if (body == null) {
            throw new IllegalStateException("네이버 지오코딩 응답이 없습니다.");
        }

        Object addresses = body.get("addresses");
        if (!(addresses instanceof List<?> addressList) || addressList.isEmpty()) {
            throw new IllegalStateException("네이버 지오코딩 결과가 없습니다.");
        }

        Object firstAddress = addressList.get(0);
        if (!(firstAddress instanceof Map<?, ?> addressMap)) {
            throw new IllegalStateException("네이버 지오코딩 주소 형식이 올바르지 않습니다.");
        }

        Object latitude = addressMap.get("y");
        Object longitude = addressMap.get("x");
        if (latitude == null || longitude == null) {
            throw new IllegalStateException("네이버 지오코딩 좌표가 없습니다.");
        }

        return GeocodingResult.of(
            new BigDecimal(latitude.toString()),
            new BigDecimal(longitude.toString())
        );
    }

    public record GeocodingResult(BigDecimal latitude, BigDecimal longitude) {

        public static GeocodingResult of(BigDecimal latitude, BigDecimal longitude) {
            return new GeocodingResult(latitude, longitude);
        }
    }
}
