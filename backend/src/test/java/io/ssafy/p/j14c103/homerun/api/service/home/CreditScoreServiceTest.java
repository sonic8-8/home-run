package io.ssafy.p.j14c103.homerun.api.service.home;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

import io.ssafy.p.j14c103.homerun.api.service.home.credit.CreditScore;
import io.ssafy.p.j14c103.homerun.api.service.home.credit.CreditScoreProvider;
import io.ssafy.p.j14c103.homerun.api.service.home.response.CreditScoreResponse;
import io.ssafy.p.j14c103.homerun.api.service.user.UserAuthContext;
import io.ssafy.p.j14c103.homerun.api.service.user.UserAuthContextService;
import io.ssafy.p.j14c103.homerun.client.ssafy.SsafyLoanClient;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialSummary;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreditScoreServiceTest {

    @Mock
    private CreditScoreProvider creditScoreProvider;

    @Mock
    private SsafyLoanClient ssafyLoanClient;

    @Mock
    private UserAuthContextService userAuthContextService;

    @Mock
    private io.ssafy.p.j14c103.homerun.api.service.financial.UserFinancialSummaryService userFinancialSummaryService;

    @InjectMocks
    private CreditScoreService creditScoreService;

  @DisplayName("SSAFY 연동 사용자는 CSS 점수와 외부 신용등급을 함께 반환한다")
  @Test
  void getCreditScore_withSsafyLink() {
    // given
    final Long userId = 1L;
    final CreditScore creditScore = CreditScore.of(300, 250, 120, 90, 80);
    final UserFinancialSummary summary = summary(userId, 10_000_000, 2_000_000, 8_000_000);

    given(userAuthContextService.getContext(userId)).willReturn(new UserAuthContext(userId, "test-user-key"));
    given(creditScoreProvider.calculate(userId)).willReturn(creditScore);
    given(userFinancialSummaryService.getSummary(userId)).willReturn(summary);
    given(ssafyLoanClient.inquireMyCreditRating("test-user-key"))
            .willReturn(Map.of("ratingName", "A", "totalAssetValue", "12345678"));

    // when
    final CreditScoreResponse response = creditScoreService.getCreditScore(userId);

    // then
    assertThat(response.getScore()).isEqualTo(840);
    assertThat(response.getGrade()).isEqualTo(2);
    assertThat(response.getRatingName()).isEqualTo("A");
        assertThat(response.getTotalAsset()).isEqualTo(12345678L);
    }

  @DisplayName("SSAFY 미연동 사용자는 CSS 점수만 반환한다")
  @Test
  void getCreditScore_withoutSsafyLink() {
    // given
    final Long userId = 1L;
    final CreditScore creditScore = CreditScore.of(280, 230, 110, 80, 70);
    final UserFinancialSummary summary = summary(userId, 8_000_000, 1_000_000, 7_000_000);

    given(userAuthContextService.getContext(userId)).willReturn(new UserAuthContext(userId, null));
    given(creditScoreProvider.calculate(userId)).willReturn(creditScore);
    given(userFinancialSummaryService.getSummary(userId)).willReturn(summary);

    // when
    final CreditScoreResponse response = creditScoreService.getCreditScore(userId);

    // then
    assertThat(response.getScore()).isEqualTo(770);
    assertThat(response.getRatingName()).isEqualTo("N/A");
    assertThat(response.getTotalAsset()).isEqualTo(8_000_000L);
        verifyNoInteractions(ssafyLoanClient);
    }

    private UserFinancialSummary summary(
        final Long userId,
        final int totalAssetAmount,
        final int totalDebtAmount,
        final int netAssetAmount
    ) {
        final UserFinancialSummary summary = UserFinancialSummary.create(userId);
        summary.refresh(totalAssetAmount, totalDebtAmount, netAssetAmount, totalAssetAmount, 0, 0);
        return summary;
    }
}
