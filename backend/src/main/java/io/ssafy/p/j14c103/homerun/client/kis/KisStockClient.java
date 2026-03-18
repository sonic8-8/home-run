package io.ssafy.p.j14c103.homerun.client.kis;

import io.ssafy.p.j14c103.homerun.config.KisApiProperties;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * 한국투자증권 OpenAPI 클라이언트.
 * 게임 시작 시 실시간 주식 현재가를 조회한다.
 *
 * API 문서: https://apiportal.koreainvestment.com
 * - 실전 도메인: https://openapi.koreainvestment.com:9443
 * - Rate limit: 초당 20건 (실전)
 */
@Slf4j
@Component
public class KisStockClient {

    private final RestClient kisRestClient;
    private final KisApiProperties kisApiProperties;

    private String cachedAccessToken;
    private LocalDateTime tokenExpiresAt;

    public KisStockClient(final RestClient kisRestClient, final KisApiProperties kisApiProperties) {
        this.kisRestClient = kisRestClient;
        this.kisApiProperties = kisApiProperties;
    }

    /**
     * 국내 주식 현재가를 조회한다.
     *
     * @param kisStockCode 한투 종목코드 (예: "005930" = 삼성전자)
     * @return 현재가 (원), 조회 실패 시 null
     */
    public Integer getCurrentPrice(final String kisStockCode) {
        try {
            final String accessToken = getAccessToken();

            final Map<?, ?> response = kisRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/uapi/domestic-stock/v1/quotations/inquire-price")
                            .queryParam("FID_COND_MRKT_DIV_CODE", "J")
                            .queryParam("FID_INPUT_ISCD", kisStockCode)
                            .build())
                    .header("authorization", "Bearer " + accessToken)
                    .header("appkey", kisApiProperties.getAppKey())
                    .header("appsecret", kisApiProperties.getAppSecret())
                    .header("tr_id", "FHKST01010100")
                    .retrieve()
                    .body(Map.class);

            if (response == null || response.get("output") == null) {
                log.warn("KIS API 응답이 비어있습니다. stockCode={}", kisStockCode);
                return null;
            }

            final Map<?, ?> output = (Map<?, ?>) response.get("output");
            final String priceStr = (String) output.get("stck_prpr");

            if (priceStr == null || priceStr.isBlank()) {
                log.warn("KIS API 현재가가 비어있습니다. stockCode={}", kisStockCode);
                return null;
            }

            return Integer.parseInt(priceStr);
        } catch (final Exception e) {
            log.error("KIS API 현재가 조회 실패. stockCode={}", kisStockCode, e);
            return null;
        }
    }

    private String getAccessToken() {
        if (cachedAccessToken != null && tokenExpiresAt != null
                && LocalDateTime.now().isBefore(tokenExpiresAt)) {
            return cachedAccessToken;
        }

        final Map<String, String> requestBody = Map.of(
                "grant_type", "client_credentials",
                "appkey", kisApiProperties.getAppKey(),
                "appsecret", kisApiProperties.getAppSecret()
        );

        final Map<?, ?> response = kisRestClient.post()
                .uri("/oauth2/tokenP")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        if (response == null || response.get("access_token") == null) {
            throw new IllegalStateException("KIS OAuth 토큰 발급 실패");
        }

        cachedAccessToken = (String) response.get("access_token");
        // 토큰 유효시간: 약 24시간, 안전하게 23시간으로 설정
        tokenExpiresAt = LocalDateTime.now().plusHours(23);

        log.info("KIS OAuth 토큰 발급 완료. 만료 예정: {}", tokenExpiresAt);
        return cachedAccessToken;
    }
}
