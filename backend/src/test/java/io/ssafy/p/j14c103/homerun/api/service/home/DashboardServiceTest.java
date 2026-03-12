package io.ssafy.p.j14c103.homerun.api.service.home;

import io.ssafy.p.j14c103.homerun.api.service.home.response.DashboardResponse;
import io.ssafy.p.j14c103.homerun.client.ssafy.SsafyDemandDepositClient;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
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
class DashboardServiceTest {

    @Mock
    private SsafyDemandDepositClient demandDepositClient;

    @InjectMocks
    private DashboardService dashboardService;

    @DisplayName("계좌 잔액을 합산하여 총자산을 반환한다")
    @Test
    void getDashboard_totalAsset() {
        final String userKey = "test-user-key";

        given(demandDepositClient.inquireAccountList(userKey))
                .willReturn(List.of(
                        Map.of("accountNo", "001", "accountBalance", "5000000"),
                        Map.of("accountNo", "002", "accountBalance", "3000000")));
        given(demandDepositClient.inquireTransactionHistory(eq(userKey), any(), any(), any()))
                .willReturn(List.of());

        final DashboardResponse response = dashboardService.getDashboard(userKey);

        assertThat(response.getTotalAsset()).isEqualTo(Money.of(8000000L));
    }

    @DisplayName("이번 달 입금 거래를 합산하여 월 수입을 반환한다")
    @Test
    void getDashboard_monthlyIncome() {
        final String userKey = "test-user-key";

        given(demandDepositClient.inquireAccountList(userKey))
                .willReturn(List.of(
                        Map.of("accountNo", "001", "accountBalance", "5000000")));
        given(demandDepositClient.inquireTransactionHistory(eq(userKey), eq("001"), any(), any()))
                .willReturn(List.of(
                        Map.of("transactionType", "1", "transactionBalance", "3000000"),
                        Map.of("transactionType", "1", "transactionBalance", "2000000"),
                        Map.of("transactionType", "2", "transactionBalance", "500000")));

        final DashboardResponse response = dashboardService.getDashboard(userKey);

        assertThat(response.getMonthlyIncome()).isEqualTo(Money.of(5000000L));
    }

    @DisplayName("이번 달 출금 거래를 합산하여 월 지출을 반환한다")
    @Test
    void getDashboard_monthlyExpense() {
        final String userKey = "test-user-key";

        given(demandDepositClient.inquireAccountList(userKey))
                .willReturn(List.of(
                        Map.of("accountNo", "001", "accountBalance", "5000000")));
        given(demandDepositClient.inquireTransactionHistory(eq(userKey), eq("001"), any(), any()))
                .willReturn(List.of(
                        Map.of("transactionType", "1", "transactionBalance", "3000000"),
                        Map.of("transactionType", "2", "transactionBalance", "500000"),
                        Map.of("transactionType", "2", "transactionBalance", "200000")));

        final DashboardResponse response = dashboardService.getDashboard(userKey);

        assertThat(response.getMonthlyExpense()).isEqualTo(Money.of(700000L));
    }

    @DisplayName("userKey가 null이면 예외가 발생한다")
    @Test
    void getDashboard_nullUserKey_exception() {
        assertThatThrownBy(() -> dashboardService.getDashboard(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("계좌가 없으면 총자산은 0원이다")
    @Test
    void getDashboard_noAccounts_zeroAsset() {
        final String userKey = "test-user-key";

        given(demandDepositClient.inquireAccountList(userKey))
                .willReturn(List.of());

        final DashboardResponse response = dashboardService.getDashboard(userKey);

        assertThat(response.getTotalAsset()).isEqualTo(Money.zero());
        assertThat(response.getMonthlyIncome()).isEqualTo(Money.zero());
        assertThat(response.getMonthlyExpense()).isEqualTo(Money.zero());
    }
}
