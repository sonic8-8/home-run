package io.ssafy.p.j14c103.homerun.api.service.pass;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import io.ssafy.p.j14c103.homerun.api.service.financial.UserFinancialSummaryService;
import io.ssafy.p.j14c103.homerun.api.service.pass.request.PassSaveServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassHistoryResponse;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassSaveResponse;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassWidgetResponse;
import io.ssafy.p.j14c103.homerun.api.service.user.UserAuthContextService;
import io.ssafy.p.j14c103.homerun.client.ssafy.SsafyDemandDepositClient;
import io.ssafy.p.j14c103.homerun.domain.account.AccountTransactionType;
import io.ssafy.p.j14c103.homerun.domain.account.AccountType;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccount;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountRepository;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountTransaction;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.pass.PassProduct;
import io.ssafy.p.j14c103.homerun.domain.pass.PassProductRepository;
import io.ssafy.p.j14c103.homerun.domain.pass.PassSubscription;
import io.ssafy.p.j14c103.homerun.domain.pass.PassSubscriptionRepository;
import io.ssafy.p.j14c103.homerun.domain.pass.UserPassTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyTransaction;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyTransactionRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PassSavingServiceTest {

    @Autowired
    private PassSavingService passSavingService;

    @Autowired
    private PassProductRepository passProductRepository;

    @Autowired
    private PassSubscriptionRepository passSubscriptionRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private UserAccountTransactionRepository userAccountTransactionRepository;

    @Autowired
    private SeedmoneyTransactionRepository seedmoneyTransactionRepository;

    @Autowired
    private UserPassTransactionRepository userPassTransactionRepository;

    @MockitoBean
    private SsafyDemandDepositClient demandDepositClient;

    @MockitoBean
    private UserAuthContextService userAuthContextService;

    @MockitoBean
    private UserFinancialSummaryService userFinancialSummaryService;

    @DisplayName("PASS 저축 시 MAIN에서 SEEDMONEY로 이체하고 거래 이력을 저장한다")
    @Test
    void save() {
        // given
        final PassSubscription subscription = saveActiveSubscription();
        saveAccounts();
        final PassSaveServiceRequest request = PassSaveServiceRequest.builder()
                .subscriptionId(subscription.getId())
                .sourceAccountId("출금계좌")
                .build();
        given(userAuthContextService.getRequiredSsafyUserKey(1L)).willReturn("test-key");
        given(demandDepositClient.transferAccount(any(), any(), any(), anyLong()))
                .willReturn(Map.of("status", "success"));
        given(demandDepositClient.inquireAccountList("test-key"))
                .willReturn(List.of(
                        Map.of("accountNo", "출금계좌", "accountBalance", "295000"),
                        Map.of("accountNo", "시드머니계좌", "accountBalance", "295000")
                ));

        // when
        final PassSaveResponse result = passSavingService.save(1L, request);

        // then
        final List<UserAccountTransaction> accountTransactions = userAccountTransactionRepository.findAll();
        final List<SeedmoneyTransaction> seedmoneyTransactions = seedmoneyTransactionRepository.findAll();

        assertThat(result.getSavedAmount()).isEqualTo(5000);
        assertThat(result.getTotalSaved()).isEqualTo(5000);
        assertThat(result.getRemainingBalance()).isEqualTo(295000);
        assertThat(accountTransactions).hasSize(2);
        assertThat(accountTransactions)
                .extracting(UserAccountTransaction::getTransactionType)
                .containsExactlyInAnyOrder(AccountTransactionType.PASS_SAVE_OUT, AccountTransactionType.PASS_SAVE_IN);
        assertThat(seedmoneyTransactions).hasSize(1);
        assertThat(seedmoneyTransactions.get(0).getTransactionType()).isEqualTo("SAVE");
        assertThat(seedmoneyTransactions.get(0).getPassId()).isEqualTo(subscription.getId());
        assertThat(userPassTransactionRepository.findAll()).hasSize(1);
        assertThat(userAccountRepository.findByUserIdAndAccountType(1L, AccountType.MAIN).orElseThrow().getBalanceSnapshot())
                .isEqualTo(295000);
        assertThat(userAccountRepository.findByUserIdAndAccountType(1L, AccountType.SEEDMONEY).orElseThrow().getBalanceSnapshot())
                .isEqualTo(295000);
        verify(demandDepositClient).transferAccount("test-key", "시드머니계좌", "출금계좌", 5000);
        verify(userFinancialSummaryService).getSummary(1L);
    }

    @DisplayName("구독 시 등록한 출금 계좌와 다른 계좌로 저축하면 예외가 발생한다")
    @Test
    void save_invalidSourceAccount_exception() {
        // given
        final PassSubscription subscription = saveActiveSubscription();
        saveAccounts();
        final PassSaveServiceRequest request = PassSaveServiceRequest.builder()
                .subscriptionId(subscription.getId())
                .sourceAccountId("다른계좌")
                .build();

        // when // then
        assertThatThrownBy(() -> passSavingService.save(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("구독 시 등록한 출금 계좌와 일치하지 않습니다.");
        verifyNoInteractions(demandDepositClient);
        assertThat(userAccountTransactionRepository.findAll()).isEmpty();
        assertThat(seedmoneyTransactionRepository.findAll()).isEmpty();
        assertThat(userPassTransactionRepository.findAll()).isEmpty();
    }

    @DisplayName("PASS 히스토리 조회 시 SAVE 거래만 반환한다")
    @Test
    void getHistory_returnsOnlySaveTransactions() {
        // given
        seedmoneyTransactionRepository.save(SeedmoneyTransaction.createSave(1L, 1L, 5000));
        seedmoneyTransactionRepository.save(SeedmoneyTransaction.createDeposit(1L, 3000, "1111"));

        // when
        final Page<PassHistoryResponse> result = passSavingService.getHistory(1L, 0, 20);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTransactionType()).isEqualTo("SAVE");
    }

    @DisplayName("위젯 조회 시 오늘과 이번 주 SAVE 금액을 집계한다")
    @Test
    void getWidget() {
        // given
        seedmoneyTransactionRepository.save(SeedmoneyTransaction.createSave(1L, 1L, 5000));
        seedmoneyTransactionRepository.save(SeedmoneyTransaction.createSave(1L, 1L, 3000));

        // when
        final PassWidgetResponse result = passSavingService.getWidget(1L);

        // then
        assertThat(result.getTodaySaved()).isEqualTo(8000);
        assertThat(result.getWeeklySaved()).isEqualTo(8000);
        assertThat(result.getWeeklyGoal()).isEqualTo(50000);
        assertThat(result.getProgressRate()).isGreaterThan(0);
        assertThat(result.getRemaining()).isEqualTo(42000);
    }

    @DisplayName("위젯 조회 시 userId가 null이면 예외가 발생한다")
    @Test
    void getWidget_nullUserId_exception() {
        // when // then
        assertThatThrownBy(() -> passSavingService.getWidget(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용자 ID는 필수입니다.");
    }

    private PassSubscription saveActiveSubscription() {
        final PassProduct product = passProductRepository.save(PassProduct.create(
                "커피 PASS",
                5000,
                "커피 한 잔 절약"
        ));
        return passSubscriptionRepository.save(
                PassSubscription.create(1L, product, 5000, "출금계좌")
        );
    }

    private void saveAccounts() {
        userAccountRepository.save(UserAccount.create(
                1L,
                AccountType.MAIN,
                "001",
                "한국은행",
                "출금계좌",
                300000
        ));
        userAccountRepository.save(UserAccount.create(
                1L,
                AccountType.SEEDMONEY,
                "001",
                "한국은행",
                "시드머니계좌",
                290000
        ));
    }
}
