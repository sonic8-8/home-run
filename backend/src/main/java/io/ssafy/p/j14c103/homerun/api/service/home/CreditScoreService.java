package io.ssafy.p.j14c103.homerun.api.service.home;

import io.ssafy.p.j14c103.homerun.api.service.financial.UserFinancialSummaryService;
import io.ssafy.p.j14c103.homerun.api.service.home.credit.CreditScore;
import io.ssafy.p.j14c103.homerun.api.service.home.credit.CreditScoreProvider;
import io.ssafy.p.j14c103.homerun.api.service.home.response.CreditScoreResponse;
import io.ssafy.p.j14c103.homerun.api.service.user.UserAuthContext;
import io.ssafy.p.j14c103.homerun.api.service.user.UserAuthContextService;
import io.ssafy.p.j14c103.homerun.client.ssafy.SsafyLoanClient;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialSummary;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreditScoreService {

    private final CreditScoreProvider creditScoreProvider;
    private final SsafyLoanClient ssafyLoanClient;
    private final UserAuthContextService userAuthContextService;
    private final UserFinancialSummaryService userFinancialSummaryService;

    public CreditScoreResponse getCreditScore(final Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        final UserAuthContext userAuthContext = userAuthContextService.getContext(userId);
        final CreditScore css = creditScoreProvider.calculate(userId);
        final UserFinancialSummary summary = userFinancialSummaryService.getSummary(userId);

        String ratingName = "N/A";
        long totalAsset = summary.getTotalAssetAmount().longValue();
        try {
            if (userAuthContext.hasSsafyUserKey()) {
                final Map<String, Object> rec = ssafyLoanClient.inquireMyCreditRating(userAuthContext.ssafyUserKey());
                ratingName = (String) rec.get("ratingName");
                totalAsset = Long.parseLong(String.valueOf(rec.get("totalAssetValue")));
            }
        } catch (Exception exception) {
            // SSAFY 조회 실패해도 CSS 점수는 반환
        }

        return CreditScoreResponse.of(
                css,
                ratingName,
                totalAsset,
                summary.getTotalDebtAmount().longValue(),
                summary.getNetAssetAmount().longValue()
        );
    }
}
