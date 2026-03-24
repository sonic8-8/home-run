package io.ssafy.p.j14c103.homerun.api.service.seedmoney;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import io.ssafy.p.j14c103.homerun.api.service.financial.UserFinancialSummaryService;
import io.ssafy.p.j14c103.homerun.api.service.seedmoney.request.SeedmoneyDepositServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.seedmoney.request.SeedmoneyTransferServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.seedmoney.response.SeedmoneyAccountResponse;
import io.ssafy.p.j14c103.homerun.api.service.user.UserAuthContextService;
import io.ssafy.p.j14c103.homerun.client.ssafy.SsafyDemandDepositClient;
import io.ssafy.p.j14c103.homerun.config.SsafyAccountProperties;
import io.ssafy.p.j14c103.homerun.domain.account.AccountType;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccount;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountRepository;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountTransaction;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyTransaction;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyTransactionRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SeedmoneyServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private UserAccountTransactionRepository userAccountTransactionRepository;

    @Mock
    private SeedmoneyTransactionRepository seedmoneyTransactionRepository;

    @Mock
    private SsafyDemandDepositClient demandDepositClient;

    @Mock
    private UserAuthContextService userAuthContextService;

    @Mock
    private UserFinancialSummaryService userFinancialSummaryService;

    @Mock
    private SsafyAccountProperties ssafyAccountProperties;

    @InjectMocks
    private SeedmoneyService seedmoneyService;

    @DisplayName("시드머니 계좌 잔액을 실시간으로 조회한다")
    @Test
    void getAccount() {
        // given
        final UserAccount account = UserAccount.create(1L, AccountType.SEEDMONEY, "001", "한국은행", "9990012345678", 0);

        given(userAccountRepository.findByUserIdAndAccountType(1L, AccountType.SEEDMONEY))
                .willReturn(Optional.of(account));
        given(userAuthContextService.getRequiredSsafyUserKey(1L)).willReturn("test-key");
        given(demandDepositClient.inquireAccountList("test-key"))
                .willReturn(List.of(Map.of("accountNo", "9990012345678", "accountBalance", "150000")));

        // when
        final SeedmoneyAccountResponse result = seedmoneyService.getAccount(1L);

        // then
        assertThat(result.getBalance()).isEqualTo(150000);
        assertThat(result.getAccountNumber()).isEqualTo("9990012345678");
        assertThat(result.getBankName()).isEqualTo("한국은행");
        verify(userFinancialSummaryService).getSummary(1L);
    }

    @DisplayName("시드머니에서 외부 계좌로 송금한다")
    @Test
    void transfer() {
        // given
        final UserAccount account = UserAccount.create(1L, AccountType.SEEDMONEY, "001", "한국은행", "시드머니계좌", 100000);
        final SeedmoneyTransferServiceRequest request = SeedmoneyTransferServiceRequest.builder()
                .amount(10000L)
                .toAccountNumber("외부계좌")
                .build();

        given(userAccountRepository.findByUserIdAndAccountType(1L, AccountType.SEEDMONEY))
                .willReturn(Optional.of(account));
        given(userAuthContextService.getRequiredSsafyUserKey(1L)).willReturn("test-key");
        given(demandDepositClient.transferAccount(any(), any(), any(), eq(10000L)))
                .willReturn(Map.of("status", "success"));
        given(demandDepositClient.inquireAccountList("test-key"))
                .willReturn(List.of(Map.of("accountNo", "시드머니계좌", "accountBalance", "90000")));
        given(seedmoneyTransactionRepository.save(any(SeedmoneyTransaction.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        seedmoneyService.transfer(1L, request);

        // then
        verify(seedmoneyTransactionRepository).save(any(SeedmoneyTransaction.class));
        verify(userAccountTransactionRepository).save(any(UserAccountTransaction.class));
        verify(userFinancialSummaryService).getSummary(1L);
    }

    @DisplayName("외부 계좌에서 시드머니로 입금한다")
    @Test
    void deposit() {
        // given
        final UserAccount account = UserAccount.create(1L, AccountType.SEEDMONEY, "001", "한국은행", "시드머니계좌", 100000);
        final SeedmoneyDepositServiceRequest request = SeedmoneyDepositServiceRequest.builder()
                .amount(20000L)
                .fromAccountNumber("외부계좌")
                .build();

        given(userAccountRepository.findByUserIdAndAccountType(1L, AccountType.SEEDMONEY))
                .willReturn(Optional.of(account));
        given(userAuthContextService.getRequiredSsafyUserKey(1L)).willReturn("test-key");
        given(demandDepositClient.transferAccount(any(), any(), any(), eq(20000L)))
                .willReturn(Map.of("status", "success"));
        given(demandDepositClient.inquireAccountList("test-key"))
                .willReturn(List.of(Map.of("accountNo", "시드머니계좌", "accountBalance", "120000")));
        given(seedmoneyTransactionRepository.save(any(SeedmoneyTransaction.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        seedmoneyService.deposit(1L, request);

        // then
        verify(seedmoneyTransactionRepository).save(any(SeedmoneyTransaction.class));
        verify(userAccountTransactionRepository).save(any(UserAccountTransaction.class));
        verify(userFinancialSummaryService).getSummary(1L);
    }

    @DisplayName("시드머니 계좌가 없으면 조회 시 예외가 발생한다")
    @Test
    void getAccount_noAccount_exception() {
        // given
        given(userAccountRepository.findByUserIdAndAccountType(1L, AccountType.SEEDMONEY))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> seedmoneyService.getAccount(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("시드머니 계좌가 없습니다");
    }

    @DisplayName("사용자 ID가 null이면 예외가 발생한다")
    @Test
    void getAccount_nullUserId_exception() {
        assertThatThrownBy(() -> seedmoneyService.getAccount(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
