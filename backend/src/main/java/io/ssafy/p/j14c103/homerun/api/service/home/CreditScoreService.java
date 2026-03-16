package io.ssafy.p.j14c103.homerun.api.service.home;

import io.ssafy.p.j14c103.homerun.api.service.home.response.CreditScoreResponse;
import io.ssafy.p.j14c103.homerun.client.ssafy.SsafyLoanClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CreditScoreService {

    private final SsafyLoanClient ssafyLoanClient;

    public CreditScoreResponse getCreditScore(final String userKey) {
        if (userKey == null || userKey.isBlank()) {
            throw new IllegalArgumentException("userKey는 필수입니다.");
        }

        final Map<String, Object> rec = ssafyLoanClient.inquireMyCreditRating(userKey);

        return CreditScoreResponse.of(
                (String) rec.get("ratingName"),
                Long.parseLong(String.valueOf(rec.get("demandDepositAssetValue"))),
                Long.parseLong(String.valueOf(rec.get("depositSavingsAssetValue"))),
                Long.parseLong(String.valueOf(rec.get("totalAssetValue")))
        );
    }
}
