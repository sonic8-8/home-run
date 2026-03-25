package io.ssafy.p.j14c103.homerun.api.service.home;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import io.ssafy.p.j14c103.homerun.api.service.home.credit.CreditScore;
import io.ssafy.p.j14c103.homerun.api.service.home.credit.CreditScoreProvider;
import io.ssafy.p.j14c103.homerun.api.service.home.response.LoanRecommendationItem;
import io.ssafy.p.j14c103.homerun.api.service.home.response.LoanRecommendationResponse;
import io.ssafy.p.j14c103.homerun.client.fss.FssLoanClient;
import io.ssafy.p.j14c103.homerun.client.fss.FssLoanResponse;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoanRecommendationServiceTest {

    @Mock
    private FssLoanClient fssLoanClient;

    @Mock
    private CreditScoreProvider creditScoreProvider;

    @InjectMocks
    private LoanRecommendationService loanRecommendationService;

    @DisplayName("개인신용대출은 CSS 점수 구간에 맞는 대출금리를 예상금리로 사용한다")
    @Test
    void getRecommendations_creditLoanUsesScoreBandRate() {
        // given
        given(creditScoreProvider.calculate(1L)).willReturn(creditScore(840, 2, "Very Good"));
        given(fssLoanClient.getCreditLoanProducts()).willReturn(creditLoanResponse());
        given(fssLoanClient.getRentHouseLoanProducts()).willReturn(FssLoanResponse.empty());
        given(fssLoanClient.getMortgageLoanProducts()).willReturn(FssLoanResponse.empty());

        // when
        final LoanRecommendationResponse response = loanRecommendationService.getRecommendations(1L);

        // then
        assertThat(response.getCreditLoans()).hasSize(1);
        final LoanRecommendationItem item = response.getCreditLoans().get(0);
        assertThat(item.getEstimatedRate()).isEqualTo(3.25);
        assertThat(item.getMinRate()).isEqualTo(3.12);
        assertThat(item.getMaxRate()).isEqualTo(12.0);
        assertThat(item.getCreditProductTypeName()).isEqualTo("일반신용대출");
        assertThat(item.getJoinWay()).isEqualTo("영업점,인터넷,스마트폰");
        assertThat(response.getEstimatedMinRate()).isEqualTo(3.25);
    }

    @DisplayName("개인신용대출은 선택 점수구간이 비어 있으면 평균 금리로 대체한다")
    @Test
    void getRecommendations_creditLoanFallsBackToAverageRate() {
        // given
        given(creditScoreProvider.calculate(1L)).willReturn(creditScore(840, 2, "Very Good"));
        given(fssLoanClient.getCreditLoanProducts()).willReturn(creditLoanResponseWithNullBand());
        given(fssLoanClient.getRentHouseLoanProducts()).willReturn(FssLoanResponse.empty());
        given(fssLoanClient.getMortgageLoanProducts()).willReturn(FssLoanResponse.empty());

        // when
        final LoanRecommendationResponse response = loanRecommendationService.getRecommendations(1L);

        // then
        assertThat(response.getCreditLoans()).hasSize(1);
        assertThat(response.getCreditLoans().get(0).getEstimatedRate()).isEqualTo(4.62);
    }

    @DisplayName("전세자금대출은 예상금리가 가장 낮은 옵션을 대표 옵션으로 선택한다")
    @Test
    void getRecommendations_jeonseLoanSelectsRepresentativeOption() {
        // given
        given(creditScoreProvider.calculate(1L)).willReturn(creditScore(840, 2, "Very Good"));
        given(fssLoanClient.getCreditLoanProducts()).willReturn(FssLoanResponse.empty());
        given(fssLoanClient.getRentHouseLoanProducts()).willReturn(jeonseLoanResponse());
        given(fssLoanClient.getMortgageLoanProducts()).willReturn(FssLoanResponse.empty());

        // when
        final LoanRecommendationResponse response = loanRecommendationService.getRecommendations(1L);

        // then
        assertThat(response.getJeonseLoans()).hasSize(1);
        final LoanRecommendationItem item = response.getJeonseLoans().get(0);
        assertThat(item.getEstimatedRate()).isEqualTo(3.16);
        assertThat(item.getAverageRate()).isEqualTo(2.94);
        assertThat(item.getRateTypeName()).isEqualTo("변동금리");
        assertThat(item.getRepaymentTypeName()).isEqualTo("만기일시상환방식");
        assertThat(item.getLoanLimit()).isEqualTo("최대3억원");
        assertThat(item.getJoinWay()).isEqualTo("영업점,모집인");
    }

    @DisplayName("주택담보대출은 대표 옵션의 담보유형과 상환조건을 함께 반환한다")
    @Test
    void getRecommendations_mortgageLoanIncludesRepresentativeOptionFields() {
        // given
        given(creditScoreProvider.calculate(1L)).willReturn(creditScore(840, 2, "Very Good"));
        given(fssLoanClient.getCreditLoanProducts()).willReturn(FssLoanResponse.empty());
        given(fssLoanClient.getRentHouseLoanProducts()).willReturn(FssLoanResponse.empty());
        given(fssLoanClient.getMortgageLoanProducts()).willReturn(mortgageLoanResponse());

        // when
        final LoanRecommendationResponse response = loanRecommendationService.getRecommendations(1L);

        // then
        assertThat(response.getMortgageLoans()).hasSize(1);
        final LoanRecommendationItem item = response.getMortgageLoans().get(0);
        assertThat(item.getEstimatedRate()).isEqualTo(2.57);
        assertThat(item.getAverageRate()).isEqualTo(2.88);
        assertThat(item.getMortgageTypeName()).isEqualTo("아파트");
        assertThat(item.getRepaymentTypeName()).isEqualTo("분할상환방식");
        assertThat(item.getRateTypeName()).isEqualTo("변동금리");
        assertThat(item.getLoanLimit()).isEqualTo("LTV 최대 70% 최대 대출한도 : 2,000백만원");
        assertThat(item.getJoinWay()).isEqualTo("영업점");
    }

    @DisplayName("개인신용대출이 비어 있으면 예상 최저금리는 0을 반환한다")
    @Test
    void getRecommendations_estimatedMinRateZeroWhenNoCreditLoans() {
        // given
        given(creditScoreProvider.calculate(1L)).willReturn(creditScore(840, 2, "Very Good"));
        given(fssLoanClient.getCreditLoanProducts()).willReturn(FssLoanResponse.empty());
        given(fssLoanClient.getRentHouseLoanProducts()).willReturn(jeonseLoanResponse());
        given(fssLoanClient.getMortgageLoanProducts()).willReturn(FssLoanResponse.empty());

        // when
        final LoanRecommendationResponse response = loanRecommendationService.getRecommendations(1L);

        // then
        assertThat(response.getEstimatedMinRate()).isZero();
    }

    private CreditScore creditScore(final int score, final int grade, final String gradeLabel) {
        return CreditScore.builder()
                .score(score)
                .grade(grade)
                .gradeLabel(gradeLabel)
                .paymentHistory(0)
                .amountsOwed(0)
                .creditLength(0)
                .creditMix(0)
                .newCredit(0)
                .build();
    }

    private FssLoanResponse creditLoanResponse() {
        return FssLoanResponse.of(
                List.of(Map.of(
                        "fin_co_no", "0010001",
                        "fin_prdt_cd", "CR0001B",
                        "kor_co_nm", "우리은행",
                        "fin_prdt_nm", "개인신용대출",
                        "join_way", "영업점,인터넷,스마트폰",
                        "crdt_prdt_type_nm", "일반신용대출"
                )),
                List.of(
                        Map.of(
                                "fin_co_no", "0010001",
                                "fin_prdt_cd", "CR0001B",
                                "crdt_lend_rate_type", "A",
                                "crdt_lend_rate_type_nm", "대출금리",
                                "crdt_grad_1", 3.12,
                                "crdt_grad_4", 3.25,
                                "crdt_grad_5", 3.34,
                                "crdt_grad_6", 3.61,
                                "crdt_grad_10", 4.09,
                                "crdt_grad_11", 5.86,
                                "crdt_grad_12", 2.56,
                                "crdt_grad_13", 12.00,
                                "crdt_grad_avg", 3.22
                        ),
                        Map.of(
                                "fin_co_no", "0010001",
                                "fin_prdt_cd", "CR0001B",
                                "crdt_lend_rate_type", "B",
                                "crdt_lend_rate_type_nm", "기준금리",
                                "crdt_grad_1", 0.80,
                                "crdt_grad_4", 0.82,
                                "crdt_grad_5", 0.81,
                                "crdt_grad_6", 0.81,
                                "crdt_grad_10", 0.83,
                                "crdt_grad_11", 0.87,
                                "crdt_grad_12", 1.20,
                                "crdt_grad_13", 0.89,
                                "crdt_grad_avg", 0.81
                        )
                )
        );
    }

    private FssLoanResponse creditLoanResponseWithNullBand() {
        return FssLoanResponse.of(
                List.of(Map.of(
                        "fin_co_no", "0010002",
                        "fin_prdt_cd", "SC001217_1",
                        "kor_co_nm", "한국스탠다드차타드은행",
                        "fin_prdt_nm", "개인신용대출",
                        "join_way", "영업점,스마트폰",
                        "crdt_prdt_type_nm", "마이너스한도대출"
                )),
                List.of(Map.of(
                        "fin_co_no", "0010002",
                        "fin_prdt_cd", "SC001217_1",
                        "crdt_lend_rate_type", "A",
                        "crdt_lend_rate_type_nm", "대출금리",
                        "crdt_grad_1", 4.49,
                        "crdt_grad_5", 6.67,
                        "crdt_grad_6", 6.79,
                        "crdt_grad_avg", 4.62
                ))
        );
    }

    private FssLoanResponse jeonseLoanResponse() {
        return FssLoanResponse.of(
                List.of(Map.of(
                        "fin_co_no", "0010001",
                        "fin_prdt_cd", "203105601",
                        "kor_co_nm", "우리은행",
                        "fin_prdt_nm", "우리전세론(주택보증)",
                        "join_way", "영업점,모집인",
                        "loan_lmt", "최대3억원"
                )),
                List.of(
                        Map.of(
                                "fin_co_no", "0010001",
                                "fin_prdt_cd", "203105601",
                                "rpay_type_nm", "만기일시상환방식",
                                "lend_rate_type_nm", "고정금리",
                                "lend_rate_min", 2.97,
                                "lend_rate_max", 4.82,
                                "lend_rate_avg", 4.06
                        ),
                        Map.of(
                                "fin_co_no", "0010001",
                                "fin_prdt_cd", "203105601",
                                "rpay_type_nm", "만기일시상환방식",
                                "lend_rate_type_nm", "변동금리",
                                "lend_rate_min", 2.97,
                                "lend_rate_max", 4.82,
                                "lend_rate_avg", 2.94
                        )
                )
        );
    }

    private FssLoanResponse mortgageLoanResponse() {
        return FssLoanResponse.of(
                List.of(Map.of(
                        "fin_co_no", "0010002",
                        "fin_prdt_cd", "SC002111/SC002015",
                        "kor_co_nm", "한국스탠다드차타드은행",
                        "fin_prdt_nm", "주택담보대출",
                        "join_way", "영업점",
                        "loan_lmt", "LTV 최대 70%\n최대 대출한도 : 2,000백만원"
                )),
                List.of(
                        Map.of(
                                "fin_co_no", "0010002",
                                "fin_prdt_cd", "SC002111/SC002015",
                                "mrtg_type_nm", "아파트",
                                "rpay_type_nm", "분할상환방식",
                                "lend_rate_type_nm", "고정금리",
                                "lend_rate_min", 2.46,
                                "lend_rate_max", 4.50,
                                "lend_rate_avg", 3.20
                        ),
                        Map.of(
                                "fin_co_no", "0010002",
                                "fin_prdt_cd", "SC002111/SC002015",
                                "mrtg_type_nm", "아파트",
                                "rpay_type_nm", "분할상환방식",
                                "lend_rate_type_nm", "변동금리",
                                "lend_rate_min", 2.46,
                                "lend_rate_max", 4.50,
                                "lend_rate_avg", 2.88
                        )
                )
        );
    }
}
