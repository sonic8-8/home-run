package io.ssafy.p.j14c103.homerun.api.service.pass;

import io.ssafy.p.j14c103.homerun.api.controller.pass.request.PassSaveRequest;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassSaveResponse;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassWidgetResponse;
import io.ssafy.p.j14c103.homerun.api.service.user.UserAuthContextService;
import io.ssafy.p.j14c103.homerun.client.ssafy.SsafyDemandDepositClient;
import io.ssafy.p.j14c103.homerun.domain.pass.PassProduct;
import io.ssafy.p.j14c103.homerun.domain.pass.PassSubscription;
import io.ssafy.p.j14c103.homerun.domain.pass.PassSubscriptionRepository;
import io.ssafy.p.j14c103.homerun.domain.pass.UserPassTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyAccount;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyAccountRepository;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyTransaction;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyTransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PassSavingServiceTest {

    @Mock
    private PassSubscriptionRepository passSubscriptionRepository;

    @Mock
    private SeedmoneyAccountRepository seedmoneyAccountRepository;

    @Mock
    private SeedmoneyTransactionRepository seedmoneyTransactionRepository;

    @Mock
    private UserPassTransactionRepository userPassTransactionRepository;

    @Mock
    private SsafyDemandDepositClient demandDepositClient;

    @Mock
    private UserAuthContextService userAuthContextService;

    @InjectMocks
    private PassSavingService passSavingService;

  @DisplayName("꾹 저축 시 SSAFY 이체 API를 호출하고 응답을 반환한다")
  @Test
  void save_success() {
    // given
    final PassProduct product = PassProduct.create("커피 PASS", 5000, "커피 한 잔 절약");
    final PassSubscription subscription = PassSubscription.create(1L, product, 5000, "출금계좌");
        ReflectionTestUtils.setField(subscription, "id", 1L);
        final SeedmoneyAccount account = SeedmoneyAccount.create(1L, "한국은행", "시드머니계좌");
        final PassSaveRequest request = new PassSaveRequest(1L, "출금계좌");

        given(passSubscriptionRepository.findById(1L)).willReturn(Optional.of(subscription));
        given(seedmoneyAccountRepository.findByUserId(1L)).willReturn(Optional.of(account));
        given(userAuthContextService.getRequiredSsafyUserKey(1L)).willReturn("test-key");
        given(demandDepositClient.transferAccount(any(), any(), any(), anyLong()))
                .willReturn(Map.of("status", "success"));
    given(seedmoneyTransactionRepository.findByUserIdAndTransactionTypeAndCreatedAtAfter(any(), any(), any()))
            .willReturn(Collections.emptyList());
    given(demandDepositClient.inquireAccountList("test-key"))
            .willReturn(List.of(Map.of("accountNo", "시드머니계좌", "accountBalance", "295000")));

    // when
    final PassSaveResponse result = passSavingService.save(1L, request);

    // then
    assertThat(result.getSavedAmount()).isEqualTo(5000);
    assertThat(result.getRemainingBalance()).isEqualTo(295000);
    verify(seedmoneyTransactionRepository).save(any(SeedmoneyTransaction.class));
    }

  @DisplayName("해지된 구독으로 저축하면 예외가 발생한다")
  @Test
  void save_canceledSubscription_exception() {
    // given
    final PassProduct product = PassProduct.create("커피 PASS", 5000, "커피 한 잔 절약");
    final PassSubscription subscription = PassSubscription.create(1L, product, 5000, "출금계좌");
        subscription.cancel();
        final PassSaveRequest request = new PassSaveRequest(1L, "출금계좌");

    given(passSubscriptionRepository.findById(1L)).willReturn(Optional.of(subscription));

    // when & then
    assertThatThrownBy(() -> passSavingService.save(1L, request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("해지된 구독");
    }

  @DisplayName("위젯에서 오늘/주간 저축 현황과 목표를 반환한다")
  @Test
  void getWidget() {
    // given
    given(seedmoneyTransactionRepository
            .findByUserIdAndTransactionTypeAndCreatedAtAfter(any(), eq("SAVE"), any()))
            .willReturn(Collections.emptyList());

    // when
    final PassWidgetResponse result = passSavingService.getWidget(1L);

    // then
    assertThat(result.getTodaySaved()).isEqualTo(0);
    assertThat(result.getWeeklySaved()).isEqualTo(0);
        assertThat(result.getWeeklyGoal()).isEqualTo(50000);
        assertThat(result.getProgressRate()).isEqualTo(0);
        assertThat(result.getRemaining()).isEqualTo(50000);
    }

  @DisplayName("위젯 조회 시 userId가 null이면 예외가 발생한다")
  @Test
  void getWidget_nullUserId_exception() {
    // when & then
    assertThatThrownBy(() -> passSavingService.getWidget(null))
            .isInstanceOf(IllegalArgumentException.class);
  }
}
