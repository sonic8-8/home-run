package io.ssafy.p.j14c103.homerun.api.service.seedmoney;

import io.ssafy.p.j14c103.homerun.api.controller.seedmoney.request.SeedmoneyDepositRequest;
import io.ssafy.p.j14c103.homerun.api.controller.seedmoney.request.SeedmoneyTransferRequest;
import io.ssafy.p.j14c103.homerun.api.service.seedmoney.response.SeedmoneyAccountResponse;
import io.ssafy.p.j14c103.homerun.client.ssafy.SsafyDemandDepositClient;
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
class SeedmoneyServiceTest {

    @Mock
    private SeedmoneyAccountRepository seedmoneyAccountRepository;

    @Mock
    private SeedmoneyTransactionRepository seedmoneyTransactionRepository;

    @Mock
    private SsafyDemandDepositClient demandDepositClient;

    @InjectMocks
    private SeedmoneyService seedmoneyService;

    @DisplayName("시드머니 계좌 잔액을 실시간으로 조회한다")
    @Test
    void getAccount() {
        final SeedmoneyAccount account = SeedmoneyAccount.create(1L, "한국은행", "9990012345678");

        given(seedmoneyAccountRepository.findByUserId(1L)).willReturn(Optional.of(account));
        given(demandDepositClient.inquireAccountList("test-key"))
                .willReturn(List.of(Map.of("accountNo", "9990012345678", "accountBalance", "150000")));

        final SeedmoneyAccountResponse result = seedmoneyService.getAccount(1L, "test-key");

        assertThat(result.getBalance()).isEqualTo(150000);
        assertThat(result.getAccountNumber()).isEqualTo("9990012345678");
        assertThat(result.getBankName()).isEqualTo("한국은행");
    }

    @DisplayName("시드머니에서 외부 계좌로 송금한다")
    @Test
    void transfer() {
        final SeedmoneyAccount account = SeedmoneyAccount.create(1L, "한국은행", "시드머니계좌");
        final SeedmoneyTransferRequest request = new SeedmoneyTransferRequest(1L, "test-key", 10000L, "외부계좌");

        given(seedmoneyAccountRepository.findByUserId(1L)).willReturn(Optional.of(account));
        given(demandDepositClient.transferAccount(any(), any(), any(), eq(10000L)))
                .willReturn(Map.of("status", "success"));
        given(demandDepositClient.inquireAccountList("test-key"))
                .willReturn(List.of(Map.of("accountNo", "시드머니계좌", "accountBalance", "90000")));

        seedmoneyService.transfer(request);

        verify(seedmoneyTransactionRepository).save(any(SeedmoneyTransaction.class));
    }

    @DisplayName("외부 계좌에서 시드머니로 입금한다")
    @Test
    void deposit() {
        final SeedmoneyAccount account = SeedmoneyAccount.create(1L, "한국은행", "시드머니계좌");
        final SeedmoneyDepositRequest request = new SeedmoneyDepositRequest(1L, "test-key", 20000L, "외부계좌");

        given(seedmoneyAccountRepository.findByUserId(1L)).willReturn(Optional.of(account));
        given(demandDepositClient.transferAccount(any(), any(), any(), eq(20000L)))
                .willReturn(Map.of("status", "success"));
        given(demandDepositClient.inquireAccountList("test-key"))
                .willReturn(List.of(Map.of("accountNo", "시드머니계좌", "accountBalance", "120000")));

        seedmoneyService.deposit(request);

        verify(seedmoneyTransactionRepository).save(any(SeedmoneyTransaction.class));
    }

    @DisplayName("시드머니 계좌가 없으면 조회 시 예외가 발생한다")
    @Test
    void getAccount_noAccount_exception() {
        given(seedmoneyAccountRepository.findByUserId(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> seedmoneyService.getAccount(1L, "test-key"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("시드머니 계좌가 없습니다");
    }

    @DisplayName("사용자 ID가 null이면 예외가 발생한다")
    @Test
    void getAccount_nullUserId_exception() {
        assertThatThrownBy(() -> seedmoneyService.getAccount(null, "test-key"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
