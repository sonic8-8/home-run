package io.ssafy.p.j14c103.homerun.api.service.game.loan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import io.ssafy.p.j14c103.homerun.api.service.game.loan.response.LoanProductDetailResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.loan.response.LoanProductResponse;
import io.ssafy.p.j14c103.homerun.client.fss.FssLoanClient;
import io.ssafy.p.j14c103.homerun.client.fss.FssLoanResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class LoanProductServiceTest {

    @Autowired
    private LoanProductService loanProductService;

    @MockitoBean
    private FssLoanClient fssLoanClient;

    @Test
    @DisplayName("FSS 데이터가 없으면 빈 목록을 반환한다")
    void getProductsEmptyWhenNoFssData() {
        // given
        given(fssLoanClient.getCreditLoanProducts()).willReturn(FssLoanResponse.empty());
        given(fssLoanClient.getRentHouseLoanProducts()).willReturn(FssLoanResponse.empty());
        given(fssLoanClient.getMortgageLoanProducts()).willReturn(FssLoanResponse.empty());

        // when
        final List<LoanProductResponse> result = loanProductService.getProducts("ALL", 0, 20);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("카테고리 CREDIT은 개인신용대출만 반환한다")
    void getProductsFilterByCredit() {
        // given
        final FssLoanResponse creditResponse = createFssResponse("CREDIT_PROD", "국민은행", "국민신용대출");
        given(fssLoanClient.getCreditLoanProducts()).willReturn(creditResponse);

        // when
        final List<LoanProductResponse> result = loanProductService.getProducts("CREDIT", 0, 20);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductName()).isEqualTo("국민신용대출");
    }

    @Test
    @DisplayName("상품 상세 조회 시 존재하지 않는 상품이면 빈 Optional을 반환한다")
    void getProductDetailEmptyWhenNotFound() {
        // given
        given(fssLoanClient.getCreditLoanProducts()).willReturn(FssLoanResponse.empty());
        given(fssLoanClient.getRentHouseLoanProducts()).willReturn(FssLoanResponse.empty());
        given(fssLoanClient.getMortgageLoanProducts()).willReturn(FssLoanResponse.empty());

        // when
        final Optional<LoanProductDetailResponse> result = loanProductService.getProductDetail("NON_EXIST");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("상품 금리 조회 시 상품이 없으면 기본 금리 3.49%를 반환한다")
    void getProductRateDefaultWhenNotFound() {
        // given
        given(fssLoanClient.getCreditLoanProducts()).willReturn(FssLoanResponse.empty());
        given(fssLoanClient.getRentHouseLoanProducts()).willReturn(FssLoanResponse.empty());
        given(fssLoanClient.getMortgageLoanProducts()).willReturn(FssLoanResponse.empty());

        // when
        final double rate = loanProductService.getProductRate("NON_EXIST");

        // then
        assertThat(rate).isEqualTo(3.49);
    }

    private FssLoanResponse createFssResponse(final String productCode,
                                               final String bankName,
                                               final String productName) {
        final Map<String, Object> base = Map.of(
                "fin_co_no", "B001",
                "fin_prdt_cd", productCode,
                "kor_co_nm", bankName,
                "fin_prdt_nm", productName
        );
        final Map<String, Object> option = Map.of(
                "fin_co_no", "B001",
                "fin_prdt_cd", productCode,
                "lend_rate_min", 3.5,
                "lend_rate_max", 5.5
        );
        return FssLoanResponse.of(List.of(base), List.of(option));
    }
}
