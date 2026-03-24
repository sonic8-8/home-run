package io.ssafy.p.j14c103.homerun.api.service.card;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.card.response.CardListResponse;
import io.ssafy.p.j14c103.homerun.api.service.card.response.CardRecommendationResponse;
import io.ssafy.p.j14c103.homerun.api.service.card.response.CardTransactionListResponse;
import io.ssafy.p.j14c103.homerun.api.service.card.response.OwnedCardListResponse;
import io.ssafy.p.j14c103.homerun.domain.card.CardTransaction;
import io.ssafy.p.j14c103.homerun.domain.card.CardTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.card.CardProduct;
import io.ssafy.p.j14c103.homerun.domain.card.CardProductRepository;
import io.ssafy.p.j14c103.homerun.domain.card.OwnedCard;
import io.ssafy.p.j14c103.homerun.domain.card.OwnedCardRepository;
import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CardServiceTest {

    private static final String BENEFITS_JSON = """
            [
              {
                "categoryId": "CG-9ca85f66311a23d",
                "categoryName": "생활",
                "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
                "discountRate": 10.0,
                "exampleMerchants": ["스타벅스"]
              },
              {
                "categoryId": "CG-9ca85f66311a23d",
                "categoryName": "생활",
                "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
                "discountRate": 5.0,
                "exampleMerchants": ["배달의민족"]
              },
              {
                "categoryId": "CG-4fa85f6455cad4a",
                "categoryName": "교통",
                "categoryDescription": "(버스, 지하철, 택시)",
                "discountRate": 3.0,
                "exampleMerchants": ["택시"]
              }
            ]
            """;

    private static final String LIVING_CATEGORY_ID = "CG-9ca85f66311a23d";
    private static final String LIVING_CATEGORY_NAME = "생활";
    private static final String TRANSPORT_CATEGORY_ID = "CG-4fa85f6455cad4a";
    private static final String TRANSPORT_CATEGORY_NAME = "교통";
    private static final String TELECOM_CATEGORY_ID = "CG-7fa85f6425bc311";
    private static final String TELECOM_CATEGORY_NAME = "통신";

    @Autowired
    private CardService cardService;

    @Autowired
    private CardProductRepository cardProductRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OwnedCardRepository ownedCardRepository;

    @Autowired
    private CardTransactionRepository cardTransactionRepository;

    @DisplayName("전체 카드 조회는 활성 카드만 이름순으로 반환한다")
    @Test
    void getCards() {
        // given
        cardProductRepository.save(CardProduct.create("Charlie Card", "Issuer", "설명", 300000, 40000, BENEFITS_JSON, "c.png", true));
        cardProductRepository.save(CardProduct.create("Alpha Card", "Issuer", "설명", 300000, 40000, BENEFITS_JSON, "a.png", true));
        cardProductRepository.save(CardProduct.create("Bravo Card", "Issuer", "설명", 300000, 40000, BENEFITS_JSON, "b.png", false));

        // when
        final CardListResponse response = cardService.getCards();

        // then
        assertThat(response.getCards()).hasSize(2);
        assertThat(response.getCards().get(0).getCardName()).isEqualTo("Alpha Card");
        assertThat(response.getCards().get(1).getCardName()).isEqualTo("Charlie Card");
        assertThat(response.getCards().get(0).getActiveBenefits()).hasSize(3);
        assertThat(response.getCards().get(0).getActiveBenefits().get(0).getCategoryName()).isEqualTo("생활");
        assertThat(response.getCards().get(0).getActiveBenefits().get(1).getCategoryName()).isEqualTo("생활");
        assertThat(response.getCards().get(0).getActiveBenefits().get(1).getDiscountRate())
                .isEqualByComparingTo("5.0");
        assertThat(response.getCards().get(0).getActiveBenefits().get(2).getCategoryName()).isEqualTo("교통");
    }

    @DisplayName("카드 추천은 전월실적과 무관하게 최근 결제월 기준 예상 절약 금액 순으로 반환한다")
    @Test
    void getRecommendations() {
        // given
        final User user = userRepository.save(User.register(
                Email.of("card-user@ssafy.com"),
                "tester",
                "hashed-password"
        ));
        final CardProduct spendCardProduct = cardProductRepository.save(CardProduct.create(
                "Spend Card", "Issuer", "설명", 0, 25000, BENEFITS_JSON, "spend.png", false));
        final OwnedCard spendCard = ownedCardRepository.save(OwnedCard.create(
                user.getId(), spendCardProduct, "주카드", "1234-****", LocalDateTime.of(2026, 1, 1, 0, 0)
        ));

        cardTransactionRepository.save(CardTransaction.create(
                user.getId(), spendCard, LIVING_CATEGORY_ID, LIVING_CATEGORY_NAME, "스타벅스", 200000, LocalDate.of(2026, 2, 5)));
        cardTransactionRepository.save(CardTransaction.create(
                user.getId(), spendCard, TRANSPORT_CATEGORY_ID, TRANSPORT_CATEGORY_NAME, "카카오T", 100000, LocalDate.of(2026, 2, 7)));
        cardTransactionRepository.save(CardTransaction.create(
                user.getId(), spendCard, TELECOM_CATEGORY_ID, TELECOM_CATEGORY_NAME, "SKT", 50000, LocalDate.of(2026, 2, 10)));

        cardProductRepository.save(CardProduct.create(
                "Echo Card", "Issuer", "설명", 0, 25000,
                singleBenefitJson(TRANSPORT_CATEGORY_ID, TRANSPORT_CATEGORY_NAME, 15.0), "e.png", true));
        cardProductRepository.save(CardProduct.create(
                "Delta Card", "Issuer", "설명", 400000, 50000,
                singleBenefitJson(LIVING_CATEGORY_ID, LIVING_CATEGORY_NAME, 20.0), "d.png", true));
        cardProductRepository.save(CardProduct.create(
                "Charlie Card", "Issuer", "설명", 0, 60000, mixedBenefitJson(), "c.png", true));
        cardProductRepository.save(CardProduct.create(
                "Bravo Card", "Issuer", "설명", 0, 30000, duplicateLivingBenefitJson(), "b.png", true));
        cardProductRepository.save(CardProduct.create(
                "Alpha Card", "Issuer", "설명", 0, 50000,
                singleBenefitJson(LIVING_CATEGORY_ID, LIVING_CATEGORY_NAME, 10.0), "a.png", true));

        // when
        final CardRecommendationResponse response = cardService.getRecommendations(user.getId());

        // then
        assertThat(response.getRecommendations()).hasSize(5);
        assertThat(response.getRecommendations())
                .extracting(card -> card.getCardName())
                .containsExactly("Delta Card", "Bravo Card", "Charlie Card", "Alpha Card", "Echo Card");
    }

    @DisplayName("카드 추천은 결제내역이 없으면 이름순 상위 5건을 반환한다")
    @Test
    void getRecommendations_withoutPaymentHistory() {
        // given
        final User user = userRepository.save(User.register(
                Email.of("fallback-user@ssafy.com"),
                "tester",
                "hashed-password"
        ));
        cardProductRepository.save(CardProduct.create("Foxtrot Card", "Issuer", "설명", 300000, 40000, BENEFITS_JSON, "f.png", true));
        cardProductRepository.save(CardProduct.create("Echo Card", "Issuer", "설명", 300000, 40000, BENEFITS_JSON, "e.png", true));
        cardProductRepository.save(CardProduct.create("Delta Card", "Issuer", "설명", 300000, 40000, BENEFITS_JSON, "d.png", true));
        cardProductRepository.save(CardProduct.create("Charlie Card", "Issuer", "설명", 300000, 40000, BENEFITS_JSON, "c.png", true));
        cardProductRepository.save(CardProduct.create("Bravo Card", "Issuer", "설명", 300000, 40000, BENEFITS_JSON, "b.png", true));
        cardProductRepository.save(CardProduct.create("Alpha Card", "Issuer", "설명", 300000, 40000, BENEFITS_JSON, "a.png", true));

        // when
        final CardRecommendationResponse response = cardService.getRecommendations(user.getId());

        // then
        assertThat(response.getRecommendations()).hasSize(5);
        assertThat(response.getRecommendations().get(0).getCardName()).isEqualTo("Alpha Card");
        assertThat(response.getRecommendations().get(4).getCardName()).isEqualTo("Echo Card");
    }

    @DisplayName("카드 추천은 가장 최근 결제월만 사용한다")
    @Test
    void getRecommendations_usesLatestPaymentMonthOnly() {
        // given
        final User user = userRepository.save(User.register(
                Email.of("latest-month-user@ssafy.com"),
                "tester",
                "hashed-password"
        ));
        final CardProduct spendCardProduct = cardProductRepository.save(CardProduct.create(
                "Spend Card", "Issuer", "설명", 0, 25000, BENEFITS_JSON, "spend.png", false));
        final OwnedCard spendCard = ownedCardRepository.save(OwnedCard.create(
                user.getId(), spendCardProduct, "주카드", "1234-****", LocalDateTime.of(2026, 1, 1, 0, 0)
        ));

        cardTransactionRepository.save(CardTransaction.create(
                user.getId(), spendCard, LIVING_CATEGORY_ID, LIVING_CATEGORY_NAME, "스타벅스", 500000, LocalDate.of(2026, 1, 31)));
        cardTransactionRepository.save(CardTransaction.create(
                user.getId(), spendCard, TRANSPORT_CATEGORY_ID, TRANSPORT_CATEGORY_NAME, "카카오T", 100000, LocalDate.of(2026, 2, 3)));

        cardProductRepository.save(CardProduct.create(
                "Bravo Card", "Issuer", "설명", 0, 50000,
                singleBenefitJson(TRANSPORT_CATEGORY_ID, TRANSPORT_CATEGORY_NAME, 10.0), "b.png", true));
        cardProductRepository.save(CardProduct.create(
                "Alpha Card", "Issuer", "설명", 0, 50000,
                singleBenefitJson(LIVING_CATEGORY_ID, LIVING_CATEGORY_NAME, 10.0), "a.png", true));

        // when
        final CardRecommendationResponse response = cardService.getRecommendations(user.getId());

        // then
        assertThat(response.getRecommendations())
                .extracting(card -> card.getCardName())
                .containsExactly("Bravo Card", "Alpha Card");
    }

    @DisplayName("카드 추천은 전월실적 미달이어도 할인율 기준으로 절약 금액을 계산한다")
    @Test
    void getRecommendations_ignoresBaselinePerformanceAmount() {
        // given
        final User user = userRepository.save(User.register(
                Email.of("baseline-user@ssafy.com"),
                "tester",
                "hashed-password"
        ));
        final CardProduct spendCardProduct = cardProductRepository.save(CardProduct.create(
                "Spend Card", "Issuer", "설명", 0, 25000, BENEFITS_JSON, "spend.png", false));
        final OwnedCard spendCard = ownedCardRepository.save(OwnedCard.create(
                user.getId(), spendCardProduct, "주카드", "1234-****", LocalDateTime.of(2026, 1, 1, 0, 0)
        ));

        cardTransactionRepository.save(CardTransaction.create(
                user.getId(), spendCard, LIVING_CATEGORY_ID, LIVING_CATEGORY_NAME, "스타벅스", 100000, LocalDate.of(2026, 2, 12)));

        cardProductRepository.save(CardProduct.create(
                "Baseline Card", "Issuer", "설명", 500000, 50000,
                singleBenefitJson(LIVING_CATEGORY_ID, LIVING_CATEGORY_NAME, 20.0), "baseline.png", true));
        cardProductRepository.save(CardProduct.create(
                "Regular Card", "Issuer", "설명", 0, 50000,
                singleBenefitJson(LIVING_CATEGORY_ID, LIVING_CATEGORY_NAME, 10.0), "regular.png", true));

        // when
        final CardRecommendationResponse response = cardService.getRecommendations(user.getId());

        // then
        assertThat(response.getRecommendations())
                .extracting(card -> card.getCardName())
                .containsExactly("Baseline Card", "Regular Card");
    }

    @DisplayName("카드 추천은 최종 절약액이 같으면 raw 절약액이 큰 카드를 먼저 반환한다")
    @Test
    void getRecommendations_usesRawSavingAsTieBreaker() {
        // given
        final User user = userRepository.save(User.register(
                Email.of("tie-breaker-user@ssafy.com"),
                "tester",
                "hashed-password"
        ));
        final CardProduct spendCardProduct = cardProductRepository.save(CardProduct.create(
                "Spend Card", "Issuer", "설명", 0, 25000, BENEFITS_JSON, "spend.png", false));
        final OwnedCard spendCard = ownedCardRepository.save(OwnedCard.create(
                user.getId(), spendCardProduct, "주카드", "1234-****", LocalDateTime.of(2026, 1, 1, 0, 0)
        ));

        cardTransactionRepository.save(CardTransaction.create(
                user.getId(), spendCard, LIVING_CATEGORY_ID, LIVING_CATEGORY_NAME, "스타벅스", 500000, LocalDate.of(2026, 2, 14)));

        cardProductRepository.save(CardProduct.create(
                "High Raw Card", "Issuer", "설명", 0, 30000,
                singleBenefitJson(LIVING_CATEGORY_ID, LIVING_CATEGORY_NAME, 12.0), "high.png", true));
        cardProductRepository.save(CardProduct.create(
                "Low Raw Card", "Issuer", "설명", 0, 30000,
                singleBenefitJson(LIVING_CATEGORY_ID, LIVING_CATEGORY_NAME, 8.0), "low.png", true));
        cardProductRepository.save(CardProduct.create(
                "Under Cap Card", "Issuer", "설명", 0, 50000,
                singleBenefitJson(LIVING_CATEGORY_ID, LIVING_CATEGORY_NAME, 5.0), "under.png", true));

        // when
        final CardRecommendationResponse response = cardService.getRecommendations(user.getId());

        // then
        assertThat(response.getRecommendations())
                .extracting(card -> card.getCardName())
                .containsExactly("High Raw Card", "Low Raw Card", "Under Cap Card");
    }

    @DisplayName("카드 추천에서 존재하지 않는 사용자면 ErrorCode 기반 예외가 발생한다")
    @Test
    void getRecommendations_userNotFound() {
        // when & then
        assertThatThrownBy(() -> cardService.getRecommendations(999L))
                .isInstanceOf(HomerunException.class)
                .extracting(exception -> ((HomerunException) exception).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @DisplayName("카드 추천에서 사용자 ID가 없으면 ErrorCode 기반 예외가 발생한다")
    @Test
    void getRecommendations_userIdRequired() {
        // when & then
        assertThatThrownBy(() -> cardService.getRecommendations(null))
                .isInstanceOf(HomerunException.class)
                .extracting(exception -> ((HomerunException) exception).getErrorCode())
                .isEqualTo(ErrorCode.USER_ID_REQUIRED);
    }

    @DisplayName("내 카드 조회는 개설일 내림차순으로 활성 카드만 반환한다")
    @Test
    void getMyCards() {
        // given
        final User user = userRepository.save(User.register(
                Email.of("my-card-user@ssafy.com"),
                "tester",
                "hashed-password"
        ));
        final CardProduct alpha = cardProductRepository.save(CardProduct.create(
                "Alpha Card", "Issuer", "설명", 0, 20000, BENEFITS_JSON, "alpha.png", true));
        final CardProduct bravo = cardProductRepository.save(CardProduct.create(
                "Bravo Card", "Issuer", "설명", 0, 20000, BENEFITS_JSON, "bravo.png", true));

        ownedCardRepository.save(OwnedCard.create(
                user.getId(), alpha, "서브카드", "1111-****", LocalDateTime.of(2026, 1, 1, 0, 0)
        ));
        ownedCardRepository.save(OwnedCard.create(
                user.getId(), bravo, "주카드", "2222-****", LocalDateTime.of(2026, 2, 1, 0, 0)
        ));

        // when
        final OwnedCardListResponse response = cardService.getMyCards(user.getId());

        // then
        assertThat(response.getCards()).hasSize(2);
        assertThat(response.getCards().get(0).getCardName()).isEqualTo("Bravo Card");
        assertThat(response.getCards().get(0).getCardAlias()).isEqualTo("주카드");
        assertThat(response.getCards().get(1).getCardName()).isEqualTo("Alpha Card");
    }

    @DisplayName("내 카드 거래내역 조회는 최신 결제일 순으로 반환한다")
    @Test
    void getMyTransactions() {
        // given
        final User user = userRepository.save(User.register(
                Email.of("my-card-transaction-user@ssafy.com"),
                "tester",
                "hashed-password"
        ));
        final CardProduct alpha = cardProductRepository.save(CardProduct.create(
                "Alpha Card", "Issuer", "설명", 0, 20000, BENEFITS_JSON, "alpha.png", true));
        final OwnedCard ownedCard = ownedCardRepository.save(OwnedCard.create(
                user.getId(), alpha, "주카드", "1111-****", LocalDateTime.of(2026, 1, 1, 0, 0)
        ));

        cardTransactionRepository.save(CardTransaction.create(
                user.getId(), ownedCard, TRANSPORT_CATEGORY_ID, TRANSPORT_CATEGORY_NAME, "카카오T", 12000, LocalDate.of(2026, 2, 5)));
        cardTransactionRepository.save(CardTransaction.create(
                user.getId(), ownedCard, LIVING_CATEGORY_ID, LIVING_CATEGORY_NAME, "스타벅스", 5600, LocalDate.of(2026, 2, 7)));

        // when
        final CardTransactionListResponse response = cardService.getMyTransactions(user.getId());

        // then
        assertThat(response.getTransactions()).hasSize(2);
        assertThat(response.getTransactions().get(0).getMerchantName()).isEqualTo("스타벅스");
        assertThat(response.getTransactions().get(0).getCardName()).isEqualTo("Alpha Card");
        assertThat(response.getTransactions().get(1).getMerchantName()).isEqualTo("카카오T");
    }

    private String singleBenefitJson(final String categoryId, final String categoryName, final double discountRate) {
        return """
                [
                  {
                    "categoryId": "%s",
                    "categoryName": "%s",
                    "categoryDescription": "",
                    "discountRate": %.1f,
                    "exampleMerchants": ["sample"]
                  }
                ]
                """.formatted(categoryId, categoryName, discountRate);
    }

    private String duplicateLivingBenefitJson() {
        return """
                [
                  {
                    "categoryId": "CG-9ca85f66311a23d",
                    "categoryName": "생활",
                    "categoryDescription": "",
                    "discountRate": 12.0,
                    "exampleMerchants": ["sample"]
                  },
                  {
                    "categoryId": "CG-9ca85f66311a23d",
                    "categoryName": "생활",
                    "categoryDescription": "",
                    "discountRate": 8.0,
                    "exampleMerchants": ["sample"]
                  }
                ]
                """;
    }

    private String mixedBenefitJson() {
        return """
                [
                  {
                    "categoryId": "CG-9ca85f66311a23d",
                    "categoryName": "생활",
                    "categoryDescription": "",
                    "discountRate": 5.0,
                    "exampleMerchants": ["sample"]
                  },
                  {
                    "categoryId": "CG-4fa85f6455cad4a",
                    "categoryName": "교통",
                    "categoryDescription": "",
                    "discountRate": 10.0,
                    "exampleMerchants": ["sample"]
                  },
                  {
                    "categoryId": "CG-7fa85f6425bc311",
                    "categoryName": "통신",
                    "categoryDescription": "",
                    "discountRate": 10.0,
                    "exampleMerchants": ["sample"]
                  }
                ]
                """;
    }
}
