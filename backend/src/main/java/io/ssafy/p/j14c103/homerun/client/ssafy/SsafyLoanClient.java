package io.ssafy.p.j14c103.homerun.client.ssafy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * SSAFY 금융망 대출 관련 API 클라이언트 (도메인 F: 신용등급 조회만)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SsafyLoanClient {

    private final RestClient ssafyRestClient;
    private final SsafyApiHeaderGenerator headerGenerator;

    /**
     * 내 신용등급 조회
     * 응답: ratingName(A~E), demandDepositAssetValue, depositSavingsAssetValue, totalAssetValue
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> inquireMyCreditRating(final String userKey) {
        final Map<String, String> header = headerGenerator.generate("inquireMyCreditRating", userKey);

        final Map<String, Object> response = ssafyRestClient.post()
                .uri("/loan/inquireMyCreditRating")
                .body(Map.of("Header", header))
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new RuntimeException("SSAFY 신용등급 조회 응답이 없습니다.");
        }

        return (Map<String, Object>) response.get("REC");
    }

    /**
     * 신용등급 기준 조회 (등급별 자산 범위)
     */
    @SuppressWarnings("unchecked")
    public java.util.List<Map<String, Object>> inquireAssetBasedCreditRatingList() {
        final Map<String, String> header = headerGenerator.generate("inquireAssetBasedCreditRatingList", null);

        final Map<String, Object> response = ssafyRestClient.post()
                .uri("/loan/inquireAssetBasedCreditRatingList")
                .body(Map.of("Header", header))
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new RuntimeException("SSAFY 신용등급 기준 조회 응답이 없습니다.");
        }

        return (java.util.List<Map<String, Object>>) response.get("REC");
    }
}
