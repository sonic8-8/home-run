package io.ssafy.p.j14c103.homerun.api.service.home;

import io.ssafy.p.j14c103.homerun.api.service.home.response.DashboardResponse;
import io.ssafy.p.j14c103.homerun.api.service.user.UserAuthContextService;
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

    @Mock
    private UserAuthContextService userAuthContextService;

    @InjectMocks
    private DashboardService dashboardService;

  @DisplayName("계좌 잔액을 합산하여 총자산을 반환한다")
  @Test
  void getDashboard_totalAsset() {
    // given
    final Long userId = 1L;
    final String userKey = "test-user-key";

        given(userAuthContextService.getRequiredSsafyUserKey(userId)).willReturn(userKey);
        given(demandDepositClient.inquireAccountList(userKey))
                .willReturn(List.of(
                        Map.of("accountNo", "001", "accountBalance", "5000000"),
                        Map.of("accountNo", "002", "accountBalance", "3000000")));
    given(demandDepositClient.inquireTransactionHistory(eq(userKey), any(), any(), any()))
            .willReturn(List.of());

    // when
    final DashboardResponse response = dashboardService.getDashboard(userId);

    // then
    assertThat(response.getTotalAssets()).isEqualTo(Money.of(8000000L));
  }

  @DisplayName("이번 달 입금 거래를 합산하여 월 수입을 반환한다")
  @Test
  void getDashboard_monthlyIncome() {
    // given
    final Long userId = 1L;
    final String userKey = "test-user-key";

        given(userAuthContextService.getRequiredSsafyUserKey(userId)).willReturn(userKey);
        given(demandDepositClient.inquireAccountList(userKey))
                .willReturn(List.of(
                        Map.of("accountNo", "001", "accountBalance", "5000000")));
    given(demandDepositClient.inquireTransactionHistory(eq(userKey), eq("001"), any(), any()))
            .willReturn(List.of(
                    Map.of("transactionType", "1", "transactionBalance", "3000000"),
                    Map.of("transactionType", "1", "transactionBalance", "2000000"),
                    Map.of("transactionType", "2", "transactionBalance", "500000")));

    // when
    final DashboardResponse response = dashboardService.getDashboard(userId);

    // then
    assertThat(response.getMonthlyIncome()).isEqualTo(Money.of(5000000L));
  }

  @DisplayName("이번 달 출금 거래를 합산하여 월 지출을 반환한다")
  @Test
  void getDashboard_monthlyExpense() {
    // given
    final Long userId = 1L;
    final String userKey = "test-user-key";

        given(userAuthContextService.getRequiredSsafyUserKey(userId)).willReturn(userKey);
        given(demandDepositClient.inquireAccountList(userKey))
                .willReturn(List.of(
                        Map.of("accountNo", "001", "accountBalance", "5000000")));
    given(demandDepositClient.inquireTransactionHistory(eq(userKey), eq("001"), any(), any()))
            .willReturn(List.of(
                    Map.of("transactionType", "1", "transactionBalance", "3000000"),
                    Map.of("transactionType", "2", "transactionBalance", "500000"),
                    Map.of("transactionType", "2", "transactionBalance", "200000")));

    // when
    final DashboardResponse response = dashboardService.getDashboard(userId);

    // then
    assertThat(response.getMonthlyExpense()).isEqualTo(Money.of(700000L));
  }

  @DisplayName("userId가 null이면 예외가 발생한다")
  @Test
  void getDashboard_nullUserId_exception() {
    // when & then
    assertThatThrownBy(() -> dashboardService.getDashboard(null))
            .isInstanceOf(IllegalArgumentException.class);
  }

  @DisplayName("계좌가 없으면 총자산은 0원이다")
  @Test
  void getDashboard_noAccounts_zeroAsset() {
    // given
    final Long userId = 1L;
    final String userKey = "test-user-key";

    given(userAuthContextService.getRequiredSsafyUserKey(userId)).willReturn(userKey);
    given(demandDepositClient.inquireAccountList(userKey))
            .willReturn(List.of());

    // when
    final DashboardResponse response = dashboardService.getDashboard(userId);

    // then
    assertThat(response.getTotalAssets()).isEqualTo(Money.zero());
    assertThat(response.getMonthlyIncome()).isEqualTo(Money.zero());
    assertThat(response.getMonthlyExpense()).isEqualTo(Money.zero());
    }
}
