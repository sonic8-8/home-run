package io.ssafy.p.j14c103.homerun.api.service.home;

import io.ssafy.p.j14c103.homerun.api.dto.home.SpendingCategoryDetail;
import io.ssafy.p.j14c103.homerun.api.dto.home.SpendingResponse;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.infrastructure.ssafy.SsafyCreditCardClient;
import io.ssafy.p.j14c103.homerun.infrastructure.ssafy.SsafyDemandDepositClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SpendingServiceTest {

    @Mock
    private SsafyCreditCardClient creditCardClient;

    @Mock
    private SsafyDemandDepositClient demandDepositClient;

    @InjectMocks
    private SpendingService spendingService;

    @DisplayName("카드 결제 내역을 카테고리별로 집계한다")
    @Test
    void getSpending_cardCategories() {
        final String userKey = "test-user-key";

        given(creditCardClient.inquireSignUpCreditCardList(userKey))
                .willReturn(List.of(
                        Map.of("cardNo", "1003622654847049", "cvc", "713")));
        given(creditCardClient.inquireCreditCardTransactionList(eq(userKey), eq("1003622654847049"), eq("713"), any(),
                any()))
                .willReturn(List.of(
                        Map.of("categoryName", "생활", "transactionBalance", "350000"),
                        Map.of("categoryName", "생활", "transactionBalance", "150000"),
                        Map.of("categoryName", "교통", "transactionBalance", "120000")));
        given(demandDepositClient.inquireAccountList(userKey))
                .willReturn(List.of());

        final SpendingResponse response = spendingService.getSpending(userKey, "202603");

        assertThat(response.getTotalExpense()).isEqualTo(Money.of(620000L));
        assertThat(response.getCategories()).hasSize(2);

        final SpendingCategoryDetail living = response.getCategories().stream()
                .filter(d -> "생활".equals(d.getCategoryName()))
                .findFirst().orElseThrow();
        assertThat(living.getAmount()).isEqualTo(Money.of(500000L));
    }

    @DisplayName("수시입출금 출금은 이체 카테고리로 집계한다")
    @Test
    void getSpending_transferFromDeposit() {
        final String userKey = "test-user-key";

        given(creditCardClient.inquireSignUpCreditCardList(userKey))
                .willReturn(List.of());
        given(demandDepositClient.inquireAccountList(userKey))
                .willReturn(List.of(
                        Map.of("accountNo", "001")));
        given(demandDepositClient.inquireTransactionHistory(eq(userKey), eq("001"), any(), any()))
                .willReturn(List.of(
                        Map.of("transactionType", "2", "transactionBalance", "500000"),
                        Map.of("transactionType", "2", "transactionBalance", "300000"),
                        Map.of("transactionType", "1", "transactionBalance", "1000000")));

        final SpendingResponse response = spendingService.getSpending(userKey, "202603");

        assertThat(response.getTotalExpense()).isEqualTo(Money.of(800000L));
        assertThat(response.getCategories()).hasSize(1);
        assertThat(response.getCategories().get(0).getCategoryName()).isEqualTo("이체");
    }

    @DisplayName("카드 + 입출금 합산하여 총 지출을 반환한다")
    @Test
    void getSpending_combined() {
        final String userKey = "test-user-key";

        given(creditCardClient.inquireSignUpCreditCardList(userKey))
                .willReturn(List.of(
                        Map.of("cardNo", "card1", "cvc", "123")));
        given(creditCardClient.inquireCreditCardTransactionList(eq(userKey), eq("card1"), eq("123"), any(), any()))
                .willReturn(List.of(
                        Map.of("categoryName", "대형마트", "transactionBalance", "200000")));
        given(demandDepositClient.inquireAccountList(userKey))
                .willReturn(List.of(
                        Map.of("accountNo", "001")));
        given(demandDepositClient.inquireTransactionHistory(eq(userKey), eq("001"), any(), any()))
                .willReturn(List.of(
                        Map.of("transactionType", "2", "transactionBalance", "500000")));

        final SpendingResponse response = spendingService.getSpending(userKey, "202603");

        assertThat(response.getTotalExpense()).isEqualTo(Money.of(700000L));
        assertThat(response.getCategories()).hasSize(2);
    }

    @DisplayName("userKey가 null이면 예외가 발생한다")
    @Test
    void getSpending_nullUserKey_exception() {
        assertThatThrownBy(() -> spendingService.getSpending(null, "202603"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("month가 null이면 현재 월로 조회한다")
    @Test
    void getSpending_nullMonth_currentMonth() {
        final String userKey = "test-user-key";

        given(creditCardClient.inquireSignUpCreditCardList(userKey))
                .willReturn(List.of());
        given(demandDepositClient.inquireAccountList(userKey))
                .willReturn(List.of());

        final SpendingResponse response = spendingService.getSpending(userKey, null);

        assertThat(response.getTotalExpense()).isEqualTo(Money.zero());
        assertThat(response.getCategories()).isEmpty();
    }
}
