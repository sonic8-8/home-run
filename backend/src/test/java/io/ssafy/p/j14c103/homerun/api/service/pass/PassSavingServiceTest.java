package io.ssafy.p.j14c103.homerun.api.service.pass;

import io.ssafy.p.j14c103.homerun.api.controller.pass.request.PassSaveRequest;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassWidgetResponse;
import io.ssafy.p.j14c103.homerun.client.ssafy.SsafyDemandDepositClient;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.pass.PassProduct;
import io.ssafy.p.j14c103.homerun.domain.pass.PassSubscription;
import io.ssafy.p.j14c103.homerun.domain.pass.PassSubscriptionRepository;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyAccount;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyAccountRepository;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyTransaction;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private SsafyDemandDepositClient demandDepositClient;

    @InjectMocks
    private PassSavingService passSavingService;

    @DisplayName("꾹 저축 시 SSAFY 이체 API를 호출하고 트랜잭션을 기록한다")
    @Test
    void save_success() {
        final PassProduct product = PassProduct.create("커피 PASS", Money.of(4500), "커피 한 잔 절약");
        final PassSubscription subscription = PassSubscription.create(1L, product, "출금계좌");
        final SeedmoneyAccount account = SeedmoneyAccount.create(1L, "시드머니계좌");
        final PassSaveRequest request = new PassSaveRequest(1L, 1L, "test-key");

        given(passSubscriptionRepository.findById(1L)).willReturn(Optional.of(subscription));
        given(seedmoneyAccountRepository.findByUserId(1L)).willReturn(Optional.of(account));
        given(demandDepositClient.transferAccount(any(), any(), any(), eq(4500L)))
                .willReturn(Map.of("status", "success"));

        passSavingService.save(request);

        verify(demandDepositClient).transferAccount("test-key", "시드머니계좌", "출금계좌", 4500L);
        verify(seedmoneyTransactionRepository).save(any(SeedmoneyTransaction.class));
    }

    @DisplayName("해지된 구독으로 저축하면 예외가 발생한다")
    @Test
    void save_canceledSubscription_exception() {
        final PassProduct product = PassProduct.create("커피 PASS", Money.of(4500), "커피 한 잔 절약");
        final PassSubscription subscription = PassSubscription.create(1L, product, "출금계좌");
        subscription.cancel();
        final PassSaveRequest request = new PassSaveRequest(1L, 1L, "test-key");

        given(passSubscriptionRepository.findById(1L)).willReturn(Optional.of(subscription));

        assertThatThrownBy(() -> passSavingService.save(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("해지된 구독");
    }

    @DisplayName("오늘/주간 저축 합산을 위젯으로 반환한다")
    @Test
    void getWidget() {
        final SeedmoneyTransaction tx = SeedmoneyTransaction.createSave(1L, 1L, BigDecimal.valueOf(4500));

        given(seedmoneyTransactionRepository.findByUserIdAndTransactionTypeAndCreatedAtAfter(
                eq(1L), eq(TransactionType.SAVE), any()))
                .willReturn(List.of(tx));

        final PassWidgetResponse result = passSavingService.getWidget(1L);

        assertThat(result.getTodaySaving()).isEqualByComparingTo(BigDecimal.valueOf(4500));
        assertThat(result.getWeekSaving()).isEqualByComparingTo(BigDecimal.valueOf(4500));
    }

    @DisplayName("사용자 ID가 null이면 위젯 조회 시 예외가 발생한다")
    @Test
    void getWidget_nullUserId_exception() {
        assertThatThrownBy(() -> passSavingService.getWidget(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
