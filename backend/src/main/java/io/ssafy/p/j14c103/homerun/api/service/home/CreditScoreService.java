package io.ssafy.p.j14c103.homerun.api.service.home;

import io.ssafy.p.j14c103.homerun.api.service.home.credit.CreditScore;
import io.ssafy.p.j14c103.homerun.api.service.home.credit.CreditScoreProvider;
import io.ssafy.p.j14c103.homerun.api.service.home.response.CreditScoreResponse;
import io.ssafy.p.j14c103.homerun.client.ssafy.SsafyLoanClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CreditScoreService {

    private final CreditScoreProvider creditScoreProvider;
    private final SsafyLoanClient ssafyLoanClient;

    public CreditScoreResponse getCreditScore(final Long userId, final String userKey) {
        // 1. FICO 기반 CSS 점수 계산
        CreditScore css = creditScoreProvider.calculate(userId);

        // 2. SSAFY 원본 등급 조회 (참고용)
        String ratingName = "N/A";
        long totalAsset = 0;
        try {
            if (userKey != null && !userKey.isBlank()) {
                Map<String, Object> rec = ssafyLoanClient.inquireMyCreditRating(userKey);
                ratingName = (String) rec.get("ratingName");
                totalAsset = Long.parseLong(String.valueOf(rec.get("totalAssetValue")));
            }
        } catch (Exception e) {
            // SSAFY 조회 실패해도 CSS 점수는 반환
        }

        return CreditScoreResponse.of(css, ratingName, totalAsset);
    }
}
