package io.ssafy.p.j14c103.homerun.api.service.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.domain.account.AccountTransactionType;
import io.ssafy.p.j14c103.homerun.domain.account.AccountType;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccount;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountRepository;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MainAccountInitialHistoryServiceTest {

    @Autowired
    private MainAccountInitialHistoryService mainAccountInitialHistoryService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private UserAccountTransactionRepository userAccountTransactionRepository;

    @AfterEach
    void tearDown() {
        userAccountTransactionRepository.deleteAllInBatch();
        userAccountRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @DisplayName("주계좌 초기 원장은 현재 잔액과 정확히 일치하게 생성된다.")
    @Test
    void seedInitialHistory() {
        // given
        final User user = saveUser("seed-history@example.com");
        userAccountRepository.save(UserAccount.create(
                user.getId(),
                AccountType.MAIN,
                "001",
                "한국은행",
                "0011111111111111",
                10_000_000
        ));

        // when
        mainAccountInitialHistoryService.seedInitialHistory(user.getId());

        // then
        final UserAccount mainAccount = userAccountRepository.findByUserIdAndAccountType(user.getId(), AccountType.MAIN)
                .orElseThrow();
        assertThat(mainAccount.getMainInitialHistorySeeded()).isTrue();
        final int currentBalanceFromHistory = userAccountTransactionRepository.findAll().stream()
                .filter(transaction -> transaction.getAccountType() == AccountType.MAIN)
                .mapToInt(transaction -> transaction.getTransactionType() == AccountTransactionType.WITHDRAW
                        ? -transaction.getAmount()
                        : transaction.getAmount())
                .sum();
        assertThat(currentBalanceFromHistory).isEqualTo(10_000_000);
    }

    @DisplayName("주계좌 초기 원장은 두 번 호출해도 중복 생성되지 않는다.")
    @Test
    void seedInitialHistoryIdempotent() {
        // given
        final User user = saveUser("seed-history-idempotent@example.com");
        userAccountRepository.save(UserAccount.create(
                user.getId(),
                AccountType.MAIN,
                "001",
                "한국은행",
                "0011111111111111",
                10_000_000
        ));

        // when
        mainAccountInitialHistoryService.seedInitialHistory(user.getId());
        final long transactionCountAfterFirstSeed = userAccountTransactionRepository.count();
        mainAccountInitialHistoryService.seedInitialHistory(user.getId());

        // then
        assertThat(userAccountTransactionRepository.count()).isEqualTo(transactionCountAfterFirstSeed);
    }

    @DisplayName("주계좌가 없으면 초기 원장 시드 시 예외가 발생한다.")
    @Test
    void seedInitialHistoryWithoutMainAccount() {
        // given
        final User user = saveUser("seed-history-no-main@example.com");

        // when // then
        assertThatThrownBy(() -> mainAccountInitialHistoryService.seedInitialHistory(user.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("주계좌가 없습니다");
    }

    private User saveUser(final String email) {
        return userRepository.saveAndFlush(User.register(
                Email.of(email),
                "홍길동",
                "encoded-password"
        ));
    }
}
