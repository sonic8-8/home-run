package io.ssafy.p.j14c103.homerun.api.service.user;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.api.service.account.UserSsafyAccountSyncService;
import io.ssafy.p.j14c103.homerun.api.service.user.response.UserMeResponse;
import io.ssafy.p.j14c103.homerun.domain.account.AccountType;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccount;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountRepository;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialSummary;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialSummaryRepository;
import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class UserMeServiceTest {

    @MockitoBean
    private UserSsafyAccountSyncService userSsafyAccountSyncService;

    @Autowired
    private UserMeService userMeService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private UserFinancialSummaryRepository userFinancialSummaryRepository;

    @AfterEach
    void tearDown() {
        userFinancialSummaryRepository.deleteAllInBatch();
        userAccountRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @DisplayName("미연동 사용자는 연동 여부 false와 null 자산을 반환한다.")
    @Test
    void getMeWithUnlinkedUser() {
        // given
        final User user = userRepository.saveAndFlush(User.register(
                Email.of("user@example.com"),
                "홍길동",
                "encoded-password"
        ));

        // when
        final UserMeResponse response = userMeService.getMe(user.getId());

        // then
        assertThat(response.isAssetLinked()).isFalse();
        assertThat(response.getTotalAssetAmount()).isNull();
        assertThat(response.getNetAssetAmount()).isNull();
    }

    @DisplayName("연동 완료 사용자는 연동 여부 true와 총자산을 반환한다.")
    @Test
    void getMeWithLinkedUser() {
        // given
        final User user = userRepository.saveAndFlush(User.register(
                Email.of("user@example.com"),
                "홍길동",
                "encoded-password"
        ));
        user.linkSsafy("test-user-key", LocalDateTime.now());
        userRepository.saveAndFlush(user);
        userAccountRepository.save(UserAccount.create(
                user.getId(),
                AccountType.MAIN,
                "001",
                "한국은행",
                "1111111111111111",
                10_000_000
        ));
        userAccountRepository.save(UserAccount.create(
                user.getId(),
                AccountType.SEEDMONEY,
                "001",
                "한국은행",
                "2222222222222222",
                0
        ));
        // stale summary row should be ignored after account sync + summary recalculation
        final UserFinancialSummary summary = UserFinancialSummary.create(user.getId());
        summary.refresh(12_500_000, 0, 12_500_000, 10_000_000, 2_500_000, 0);
        userFinancialSummaryRepository.save(summary);

        // when
        final UserMeResponse response = userMeService.getMe(user.getId());

        // then
        assertThat(response.isAssetLinked()).isTrue();
        assertThat(response.getTotalAssetAmount()).isEqualTo(10_000_000);
        assertThat(response.getNetAssetAmount()).isEqualTo(10_000_000);
    }

    @DisplayName("SSAFY 연동이 있어도 계좌가 누락되면 미연동으로 본다.")
    @Test
    void getMeWithMissingAccounts() {
        // given
        final User user = userRepository.saveAndFlush(User.register(
                Email.of("user@example.com"),
                "홍길동",
                "encoded-password"
        ));
        user.linkSsafy("test-user-key", LocalDateTime.now());
        userRepository.saveAndFlush(user);
        userAccountRepository.save(UserAccount.create(
                user.getId(),
                AccountType.MAIN,
                "001",
                "한국은행",
                "1111111111111111",
                10_000_000
        ));

        // when
        final UserMeResponse response = userMeService.getMe(user.getId());

        // then
        assertThat(response.isAssetLinked()).isFalse();
        assertThat(response.getTotalAssetAmount()).isNull();
        assertThat(response.getNetAssetAmount()).isNull();
    }
}
