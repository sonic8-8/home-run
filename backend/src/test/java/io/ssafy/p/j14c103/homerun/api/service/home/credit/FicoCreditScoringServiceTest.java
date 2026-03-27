package io.ssafy.p.j14c103.homerun.api.service.home.credit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.pass.PassProduct;
import io.ssafy.p.j14c103.homerun.domain.pass.PassProductRepository;
import io.ssafy.p.j14c103.homerun.domain.pass.PassSubscription;
import io.ssafy.p.j14c103.homerun.domain.pass.PassSubscriptionRepository;
import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.HomeCreditScoreSnapshotType;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetCardSpend;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetCardSpendRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetDeposit;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetDepositRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetLoan;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetLoanRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetOtherIncome;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetOtherIncomeRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetProfile;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetProfileRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserHomeCreditScoreSnapshotRepository;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.domain.spending.SpendingCategory;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class FicoCreditScoringServiceTest {

    private static final ZoneId TEST_ZONE = ZoneId.of("Asia/Seoul");

    @Autowired
    private FicoCreditScoringService ficoCreditScoringService;

    @MockitoBean
    private Clock appClock;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAssetProfileRepository userAssetProfileRepository;

    @Autowired
    private UserAssetDepositRepository userAssetDepositRepository;

    @Autowired
    private UserAssetLoanRepository userAssetLoanRepository;

    @Autowired
    private UserAssetOtherIncomeRepository userAssetOtherIncomeRepository;

    @Autowired
    private UserAssetCardSpendRepository userAssetCardSpendRepository;

    @Autowired
    private UserHomeCreditScoreSnapshotRepository userHomeCreditScoreSnapshotRepository;

    @Autowired
    private PassProductRepository passProductRepository;

    @Autowired
    private PassSubscriptionRepository passSubscriptionRepository;

    private Long userId;

    @BeforeEach
    void setUp() {
        setNow(LocalDateTime.of(2026, 3, 27, 10, 0));
        final User user = User.register(Email.of("test@test.com"), "tester", "password");
        userRepository.save(user);
        this.userId = user.getId();
    }

    @Test
    @DisplayName("금융 이력 없는 사용자는 보수적 기본 점수를 받는다")
    void noHistoryUserGetsConservativeBaseScore() {
        // when
        final CreditScore result = ficoCreditScoringService.calculate(userId);

        // then
        assertThat(result.getPaymentHistory()).isEqualTo(175);
        assertThat(result.getAmountsOwed()).isEqualTo(200);
        assertThat(result.getCreditMix()).isEqualTo(20);
        assertThat(result.getNewCredit()).isEqualTo(100);
        assertThat(result.getScore()).isLessThan(600);
        assertThat(result.getGrade()).isEqualTo(5);
        assertThat(result.getGradeLabel()).isEqualTo("Poor");
    }

    @Test
    @DisplayName("CSS 총점은 5요소 합산이며 1000점을 초과하지 않는다")
    void totalScoreIsSumOfFiveFactorsMaxThousand() {
        // when
        final CreditScore result = ficoCreditScoringService.calculate(userId);

        // then
        final int expectedTotal = result.getPaymentHistory() + result.getAmountsOwed()
                + result.getCreditLength() + result.getCreditMix() + result.getNewCredit();
        assertThat(result.getScore()).isEqualTo(expectedTotal);
        assertThat(result.getScore()).isLessThanOrEqualTo(1000);
    }

    @Test
    @DisplayName("등급별 금리 계수는 1등급 0.0, 5등급 1.0이다")
    void rateCoefficient() {
        // given & when & then
        assertThat(CreditScore.of(350, 300, 150, 100, 100).rateCoefficient()).isEqualTo(0.0);
        assertThat(CreditScore.of(300, 250, 120, 80, 80).rateCoefficient()).isEqualTo(0.25);
        assertThat(CreditScore.of(250, 200, 100, 70, 80).rateCoefficient()).isEqualTo(0.50);
        assertThat(CreditScore.of(200, 150, 80, 50, 50).rateCoefficient()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("사용자 입력 프로필이 있으면 직장, 예금, 수입/지출, 대출 금액에 따라 점수가 달라진다")
    void assetProfileChangesScore() {
        // given
        final User stableUser = User.register(Email.of("stable@test.com"), "stable", "password");
        userRepository.save(stableUser);
        userAssetProfileRepository.save(UserAssetProfile.create(
                stableUser.getId(),
                4_000_000,
                25,
                4_500_000,
                1_500_000,
                JobType.LARGE_BIZ
        ));
        userAssetDepositRepository.save(UserAssetDeposit.create(stableUser.getId(), "정기예금", 20_000_000));
        userAssetOtherIncomeRepository.save(UserAssetOtherIncome.create(stableUser.getId(), "부업", 300_000));
        userAssetCardSpendRepository.save(UserAssetCardSpend.create(stableUser.getId(), SpendingCategory.LIVING, 200_000));

        final User riskyUser = User.register(Email.of("risky@test.com"), "risky", "password");
        userRepository.save(riskyUser);
        userAssetProfileRepository.save(UserAssetProfile.create(
                riskyUser.getId(),
                2_000_000,
                25,
                2_000_000,
                1_800_000,
                JobType.FREELANCER
        ));
        userAssetDepositRepository.save(UserAssetDeposit.create(riskyUser.getId(), "적금", 500_000));
        userAssetLoanRepository.save(UserAssetLoan.create(riskyUser.getId(), "신용대출", 20_000_000));
        userAssetCardSpendRepository.save(UserAssetCardSpend.create(riskyUser.getId(), SpendingCategory.TRANSPORT, 600_000));

        // when
        final CreditScore stableScore = ficoCreditScoringService.calculate(stableUser.getId());
        final CreditScore riskyScore = ficoCreditScoringService.calculate(riskyUser.getId());

        // then
        assertThat(stableScore.getScore()).isGreaterThan(riskyScore.getScore());
        assertThat(stableScore.getGrade()).isLessThanOrEqualTo(riskyScore.getGrade());
    }

    @Test
    @DisplayName("현재 달 CSS는 고정되고 다음 달 1일에 새 스냅샷으로 갱신된다")
    void creditScoreSnapshotIsFrozenUntilNextMonthStart() {
        // given
        userAssetProfileRepository.save(UserAssetProfile.create(
                userId,
                3_000_000,
                25,
                4_000_000,
                1_500_000,
                JobType.MID_BIZ
        ));
        userAssetDepositRepository.save(UserAssetDeposit.create(userId, "정기예금", 5_000_000));

        // when
        final CreditScore marchScore = ficoCreditScoringService.calculate(userId);

        // then
        assertThat(userHomeCreditScoreSnapshotRepository.findByUserIdAndScoreMonthStart(userId, LocalDate.of(2026, 3, 1)))
                .isPresent()
                .get()
                .extracting(snapshot -> snapshot.getSnapshotType())
                .isEqualTo(HomeCreditScoreSnapshotType.INITIAL);

        final PassProduct passProduct = passProductRepository.save(PassProduct.create("커피 PASS", 5_000, "커피값 절약"));
        final PassSubscription passSubscription = passSubscriptionRepository.save(
                PassSubscription.create(userId, passProduct, 5_000, "001-1234-5678")
        );
        ReflectionTestUtils.setField(passSubscription, "subscribedAt", LocalDateTime.of(2026, 3, 28, 12, 0));

        final CreditScore sameMonthScore = ficoCreditScoringService.calculate(userId);
        assertThat(sameMonthScore.getScore()).isEqualTo(marchScore.getScore());

        setNow(LocalDateTime.of(2026, 4, 1, 10, 0));

        final CreditScore aprilScore = ficoCreditScoringService.calculate(userId);

        assertThat(userHomeCreditScoreSnapshotRepository.findByUserIdAndScoreMonthStart(userId, LocalDate.of(2026, 4, 1)))
                .isPresent()
                .get()
                .extracting(snapshot -> snapshot.getSnapshotType())
                .isEqualTo(HomeCreditScoreSnapshotType.MONTHLY);
        assertThat(aprilScore.getScore()).isNotEqualTo(marchScore.getScore());
    }

    private void setNow(final LocalDateTime localDateTime) {
        given(appClock.getZone()).willReturn(TEST_ZONE);
        given(appClock.instant()).willReturn(localDateTime.atZone(TEST_ZONE).toInstant());
    }
}
