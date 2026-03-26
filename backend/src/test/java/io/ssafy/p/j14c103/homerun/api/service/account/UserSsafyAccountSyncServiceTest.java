package io.ssafy.p.j14c103.homerun.api.service.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import io.ssafy.p.j14c103.homerun.client.ssafy.SsafyDemandDepositClient;
import io.ssafy.p.j14c103.homerun.domain.account.AccountTransactionType;
import io.ssafy.p.j14c103.homerun.domain.account.AccountType;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccount;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountRepository;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountTransaction;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyAccountRepository;
import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class UserSsafyAccountSyncServiceTest {

    @Autowired
    private UserSsafyAccountSyncService userSsafyAccountSyncService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private UserAccountTransactionRepository userAccountTransactionRepository;

    @Autowired
    private SeedmoneyAccountRepository seedmoneyAccountRepository;

    @MockitoBean
    private SsafyDemandDepositClient ssafyDemandDepositClient;

    @AfterEach
    void tearDown() {
        userAccountTransactionRepository.deleteAllInBatch();
        seedmoneyAccountRepository.deleteAllInBatch();
        userAccountRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @DisplayName("baseline 이후 거래만 한 번 가져오고 잔액 snapshot을 갱신한다.")
    @Test
    void syncLinkedAccountsImportsOnlyAfterBaseline() {
        // given
        final User user = saveLinkedUser("sync-after-baseline@example.com");
        final UserAccount mainAccount = UserAccount.create(
                user.getId(),
                AccountType.MAIN,
                "001",
                "한국은행",
                "0011111111111111",
                10_000_000
        );
        mainAccount.initializeSsafySync("59");
        userAccountRepository.save(mainAccount);

        final UserAccount seedmoneyAccount = UserAccount.create(
                user.getId(),
                AccountType.SEEDMONEY,
                "001",
                "한국은행",
                "0012222222222222",
                0
        );
        seedmoneyAccount.initializeSsafySync(null);
        userAccountRepository.save(seedmoneyAccount);

        given(ssafyDemandDepositClient.inquireAccountList("sync-key"))
                .willReturn(List.of(
                        Map.of("accountNo", "0011111111111111", "accountBalance", "10100000"),
                        Map.of("accountNo", "0012222222222222", "accountBalance", "0")
                ));
        given(ssafyDemandDepositClient.inquireTransactionHistory(eq("sync-key"), eq("0011111111111111"), anyString(), anyString()))
                .willReturn(List.of(
                        Map.of(
                                "transactionUniqueNo", "59",
                                "transactionDate", "20260326",
                                "transactionTime", "102447",
                                "transactionType", "1",
                                "transactionTypeName", "입금",
                                "transactionAccountNo", "",
                                "transactionBalance", "10000000",
                                "transactionSummary", "(수시입출금) : 입금"
                        ),
                        Map.of(
                                "transactionUniqueNo", "60",
                                "transactionDate", "20260326",
                                "transactionTime", "102500",
                                "transactionType", "1",
                                "transactionTypeName", "입금",
                                "transactionAccountNo", "",
                                "transactionBalance", "200000",
                                "transactionSummary", "급여 입금"
                        ),
                        Map.of(
                                "transactionUniqueNo", "61",
                                "transactionDate", "20260326",
                                "transactionTime", "102700",
                                "transactionType", "2",
                                "transactionTypeName", "출금",
                                "transactionAccountNo", "",
                                "transactionBalance", "100000",
                                "transactionSummary", "생활비 지출"
                        )
                ));
        given(ssafyDemandDepositClient.inquireTransactionHistory(eq("sync-key"), eq("0012222222222222"), anyString(), anyString()))
                .willReturn(List.of());

        // when
        userSsafyAccountSyncService.syncLinkedAccounts(user.getId());
        userSsafyAccountSyncService.syncLinkedAccounts(user.getId());

        // then
        assertThat(userAccountTransactionRepository.findAll())
                .filteredOn(transaction -> transaction.getAccountType() == AccountType.MAIN)
                .hasSize(2)
                .extracting(UserAccountTransaction::getSsafyTransactionUniqueNo)
                .containsExactlyInAnyOrder("60", "61");
        assertThat(userAccountTransactionRepository.findAll())
                .filteredOn(transaction -> transaction.getAccountType() == AccountType.MAIN)
                .extracting(UserAccountTransaction::getTransactionType)
                .containsExactlyInAnyOrder(AccountTransactionType.DEPOSIT, AccountTransactionType.WITHDRAW);
        assertThat(userAccountRepository.findByUserIdAndAccountType(user.getId(), AccountType.MAIN).orElseThrow()
                .getBalanceSnapshot()).isEqualTo(10_100_000);
        assertThat(userAccountRepository.findByUserIdAndAccountType(user.getId(), AccountType.MAIN).orElseThrow()
                .getLastSyncedSsafyTransactionUniqueNo()).isEqualTo("61");
    }

    @DisplayName("자기 계좌 간 이체는 INTERNAL_TRANSFER로 저장하고 시드머니 projection도 갱신한다.")
    @Test
    void syncLinkedAccountsMapsOwnTransferAndSyncsSeedmoneyProjection() {
        // given
        final User user = saveLinkedUser("sync-internal-transfer@example.com");
        final UserAccount mainAccount = UserAccount.create(
                user.getId(),
                AccountType.MAIN,
                "001",
                "한국은행",
                "0011111111111111",
                10_000_000
        );
        mainAccount.initializeSsafySync("60");
        userAccountRepository.save(mainAccount);

        final UserAccount seedmoneyAccount = UserAccount.create(
                user.getId(),
                AccountType.SEEDMONEY,
                "001",
                "한국은행",
                "0012222222222222",
                0
        );
        seedmoneyAccount.initializeSsafySync("60");
        userAccountRepository.save(seedmoneyAccount);

        given(ssafyDemandDepositClient.inquireAccountList("sync-key"))
                .willReturn(List.of(
                        Map.of("accountNo", "0011111111111111", "accountBalance", "9500000"),
                        Map.of("accountNo", "0012222222222222", "accountBalance", "500000")
                ));
        given(ssafyDemandDepositClient.inquireTransactionHistory(eq("sync-key"), eq("0011111111111111"), anyString(), anyString()))
                .willReturn(List.of(
                        Map.of(
                                "transactionUniqueNo", "61",
                                "transactionDate", "20260326",
                                "transactionTime", "103229",
                                "transactionType", "2",
                                "transactionTypeName", "출금(이체)",
                                "transactionAccountNo", "0012222222222222",
                                "transactionBalance", "500000",
                                "transactionSummary", "(수시입출금) : 출금(이체)"
                        )
                ));
        given(ssafyDemandDepositClient.inquireTransactionHistory(eq("sync-key"), eq("0012222222222222"), anyString(), anyString()))
                .willReturn(List.of(
                        Map.of(
                                "transactionUniqueNo", "62",
                                "transactionDate", "20260326",
                                "transactionTime", "103230",
                                "transactionType", "1",
                                "transactionTypeName", "입금(이체)",
                                "transactionAccountNo", "0011111111111111",
                                "transactionBalance", "500000",
                                "transactionSummary", "(수시입출금) : 입금(이체)"
                        )
                ));

        // when
        userSsafyAccountSyncService.syncLinkedAccounts(user.getId());

        // then
        assertThat(userAccountTransactionRepository.findAll())
                .extracting(UserAccountTransaction::getTransactionType)
                .containsOnly(AccountTransactionType.INTERNAL_TRANSFER);
        assertThat(seedmoneyAccountRepository.findByUserId(user.getId())).isPresent();
        assertThat(seedmoneyAccountRepository.findByUserId(user.getId()).orElseThrow().getBalanceSnapshot())
                .isEqualTo(500000);
        assertThat(userAccountRepository.findByUserIdAndAccountType(user.getId(), AccountType.SEEDMONEY).orElseThrow()
                .getBalanceSnapshot()).isEqualTo(500000);
    }

    private User saveLinkedUser(final String email) {
        final User user = User.register(
                Email.of(email),
                "홍길동",
                "encoded-password"
        );
        user.linkSsafy("sync-key", LocalDateTime.now());
        return userRepository.saveAndFlush(user);
    }
}
