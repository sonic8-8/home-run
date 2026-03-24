package io.ssafy.p.j14c103.homerun.api.service.home.credit;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.domain.account.AccountTransactionType;
import io.ssafy.p.j14c103.homerun.domain.account.AccountType;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccount;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountRepository;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountTransaction;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.card.CardProduct;
import io.ssafy.p.j14c103.homerun.domain.card.CardProductRepository;
import io.ssafy.p.j14c103.homerun.domain.card.CardTransaction;
import io.ssafy.p.j14c103.homerun.domain.card.CardTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.card.OwnedCard;
import io.ssafy.p.j14c103.homerun.domain.card.OwnedCardRepository;
import io.ssafy.p.j14c103.homerun.domain.financial.FinancialProductType;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialProduct;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialProductRepository;
import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class FicoCreditScoringServiceTest {

    @Autowired
    private FicoCreditScoringService ficoCreditScoringService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private UserAccountTransactionRepository userAccountTransactionRepository;

    @Autowired
    private CardProductRepository cardProductRepository;

    @Autowired
    private OwnedCardRepository ownedCardRepository;

    @Autowired
    private CardTransactionRepository cardTransactionRepository;

    @Autowired
    private UserFinancialProductRepository userFinancialProductRepository;

    private Long userId;

    @BeforeEach
    void setUp() {
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
        final int expectedTotal = result.getPaymentHistory()
                + result.getAmountsOwed()
                + result.getCreditLength()
                + result.getCreditMix()
                + result.getNewCredit();
        assertThat(result.getScore()).isEqualTo(expectedTotal);
        assertThat(result.getScore()).isLessThanOrEqualTo(1000);
    }

    @Test
    @DisplayName("등급별 금리 계수는 1등급 0.0, 5등급 1.0이다")
    void rateCoefficient() {
        assertThat(CreditScore.of(350, 300, 150, 100, 100).rateCoefficient()).isEqualTo(0.0);
        assertThat(CreditScore.of(300, 250, 120, 80, 80).rateCoefficient()).isEqualTo(0.25);
        assertThat(CreditScore.of(250, 200, 100, 70, 80).rateCoefficient()).isEqualTo(0.50);
        assertThat(CreditScore.of(200, 150, 80, 50, 50).rateCoefficient()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("MAIN과 SEEDMONEY 계좌가 있으면 credit mix와 credit length가 반영된다")
    void accountsAffectCreditMixAndCreditLength() {
        // given
        userAccountRepository.save(UserAccount.create(
                userId,
                AccountType.MAIN,
                "001",
                "한국은행",
                "0011111111111111",
                100000
        ));
        userAccountRepository.save(UserAccount.create(
                userId,
                AccountType.SEEDMONEY,
                "001",
                "한국은행",
                "0012222222222222",
                50000
        ));

        // when
        final CreditScore result = ficoCreditScoringService.calculate(userId);

        // then
        assertThat(result.getCreditMix()).isGreaterThan(20);
        assertThat(result.getCreditLength()).isGreaterThanOrEqualTo(15);
    }

    @Test
    @DisplayName("보유 카드와 카드 사용 이력이 있으면 payment history가 로컬 카드 데이터로 계산된다")
    void localCardDataAffectsPaymentHistory() {
        // given
        userAccountRepository.save(UserAccount.create(
                userId,
                AccountType.MAIN,
                "001",
                "한국은행",
                "0011111111111111",
                100000
        ));
        final CardProduct cardProduct = cardProductRepository.save(CardProduct.create(
                "Alpha Card", "Issuer", "설명", 0, 20000, "[]", "alpha.png", true
        ));
        final OwnedCard ownedCard = ownedCardRepository.save(OwnedCard.create(
                userId,
                cardProduct,
                "주카드",
                "1111-****",
                LocalDateTime.now().minusMonths(3)
        ));
        cardTransactionRepository.save(CardTransaction.create(
                userId,
                ownedCard,
                "CG-1",
                "생활",
                "스타벅스",
                12000,
                LocalDate.now().minusDays(2)
        ));

        // when
        final CreditScore result = ficoCreditScoringService.calculate(userId);

        // then
        assertThat(result.getPaymentHistory()).isEqualTo(350);
        assertThat(result.getCreditMix()).isGreaterThan(20);
    }

    @Test
    @DisplayName("계좌 입출금과 대출 상품은 amounts owed, credit mix, new credit에 반영된다")
    void localAssetsAffectAmountsOwedAndNewCredit() {
        // given
        userAccountRepository.save(UserAccount.create(
                userId,
                AccountType.MAIN,
                "001",
                "한국은행",
                "0011111111111111",
                3000000
        ));
        userAccountRepository.save(UserAccount.create(
                userId,
                AccountType.SEEDMONEY,
                "001",
                "한국은행",
                "0012222222222222",
                1000000
        ));
        userAccountTransactionRepository.save(UserAccountTransaction.create(
                userId,
                AccountType.MAIN,
                null,
                AccountTransactionType.DEPOSIT,
                3000000,
                null
        ));
        userAccountTransactionRepository.save(UserAccountTransaction.create(
                userId,
                AccountType.MAIN,
                null,
                AccountTransactionType.WITHDRAW,
                600000,
                null
        ));
        final CardProduct cardProduct = cardProductRepository.save(CardProduct.create(
                "Alpha Card", "Issuer", "설명", 0, 20000, "[]", "alpha.png", true
        ));
        ownedCardRepository.save(OwnedCard.create(
                userId,
                cardProduct,
                "주카드",
                "1111-****",
                LocalDateTime.now().minusMonths(1)
        ));
        userFinancialProductRepository.save(UserFinancialProduct.create(
                userId,
                FinancialProductType.LOAN,
                "한국은행",
                "신용대출",
                500000,
                LocalDateTime.now().minusMonths(2)
        ));

        // when
        final CreditScore result = ficoCreditScoringService.calculate(userId);

        // then
        assertThat(result.getAmountsOwed()).isNotEqualTo(200);
        assertThat(result.getCreditMix()).isGreaterThan(40);
        assertThat(result.getNewCredit()).isLessThan(100);
    }
}
