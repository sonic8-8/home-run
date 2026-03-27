package io.ssafy.p.j14c103.homerun.api.service.card;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import io.ssafy.p.j14c103.homerun.api.service.card.response.CardListResponse;
import io.ssafy.p.j14c103.homerun.api.service.card.response.CardRecommendationResponse;
import io.ssafy.p.j14c103.homerun.api.service.image.ImageStorageService;
import io.ssafy.p.j14c103.homerun.domain.card.CardProduct;
import io.ssafy.p.j14c103.homerun.domain.card.CardProductRepository;
import io.ssafy.p.j14c103.homerun.domain.paymenthistory.MemberPaymentHistory;
import io.ssafy.p.j14c103.homerun.domain.paymenthistory.MemberPaymentHistoryRepository;
import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

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
    private MemberPaymentHistoryRepository memberPaymentHistoryRepository;

    @MockitoBean
    private ImageStorageService imageStorageService;

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

        memberPaymentHistoryRepository.save(MemberPaymentHistory.create(
                user.getId(), LIVING_CATEGORY_ID, LIVING_CATEGORY_NAME, "스타벅스", 200000, LocalDate.of(2026, 2, 5)));
        memberPaymentHistoryRepository.save(MemberPaymentHistory.create(
                user.getId(), TRANSPORT_CATEGORY_ID, TRANSPORT_CATEGORY_NAME, "카카오T", 100000, LocalDate.of(2026, 2, 7)));
        memberPaymentHistoryRepository.save(MemberPaymentHistory.create(
                user.getId(), TELECOM_CATEGORY_ID, TELECOM_CATEGORY_NAME, "SKT", 50000, LocalDate.of(2026, 2, 10)));

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

        memberPaymentHistoryRepository.save(MemberPaymentHistory.create(
                user.getId(), LIVING_CATEGORY_ID, LIVING_CATEGORY_NAME, "스타벅스", 500000, LocalDate.of(2026, 1, 31)));
        memberPaymentHistoryRepository.save(MemberPaymentHistory.create(
                user.getId(), TRANSPORT_CATEGORY_ID, TRANSPORT_CATEGORY_NAME, "카카오T", 100000, LocalDate.of(2026, 2, 3)));

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

        memberPaymentHistoryRepository.save(MemberPaymentHistory.create(
                user.getId(), LIVING_CATEGORY_ID, LIVING_CATEGORY_NAME, "스타벅스", 100000, LocalDate.of(2026, 2, 12)));

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

        memberPaymentHistoryRepository.save(MemberPaymentHistory.create(
                user.getId(), LIVING_CATEGORY_ID, LIVING_CATEGORY_NAME, "스타벅스", 500000, LocalDate.of(2026, 2, 14)));

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

    @DisplayName("카드 추천 v2는 회원가입 소비분야만 사용해 카드를 추천한다")
    @Test
    void getPreferenceRecommendations_usesPaymentTypeOnly() {
        // given
        final User user = userRepository.save(User.register(
                Email.of("preference-user@ssafy.com"),
                "tester",
                "hashed-password"
        ));
        user.updatePaymentType("LIVING");
        userRepository.flush();

        memberPaymentHistoryRepository.save(MemberPaymentHistory.create(
                user.getId(), TRANSPORT_CATEGORY_ID, TRANSPORT_CATEGORY_NAME, "카카오T", 900000, LocalDate.of(2026, 2, 20)));

        cardProductRepository.save(CardProduct.create(
                "Transport Max", "Issuer", "설명", 0, 50000,
                singleBenefitJson(TRANSPORT_CATEGORY_ID, TRANSPORT_CATEGORY_NAME, 20.0), "transport.png", true));
        cardProductRepository.save(CardProduct.create(
                "Living Pick", "Issuer", "설명", 0, 50000,
                singleBenefitJson(LIVING_CATEGORY_ID, LIVING_CATEGORY_NAME, 10.0), "living.png", true));

        // when
        final CardRecommendationResponse response = cardService.getPreferenceRecommendations(user.getId());

        // then
        assertThat(response.getRecommendations())
                .extracting(card -> card.getCardName())
                .containsExactly("Living Pick");
    }

    @DisplayName("카드 추천 v2는 최고 할인율이 같으면 매칭 혜택 개수와 카드명으로 정렬한다")
    @Test
    void getPreferenceRecommendations_usesMatchCountAndCardNameAsTieBreaker() {
        // given
        final User user = userRepository.save(User.register(
                Email.of("preference-tie-user@ssafy.com"),
                "tester",
                "hashed-password"
        ));
        user.updatePaymentType("LIVING,TRANSPORT");
        userRepository.flush();

        cardProductRepository.save(CardProduct.create(
                "Bravo Single", "Issuer", "설명", 0, 50000,
                singleBenefitJson(LIVING_CATEGORY_ID, LIVING_CATEGORY_NAME, 12.0), "bravo.png", true));
        cardProductRepository.save(CardProduct.create(
                "Alpha Dual", "Issuer", "설명", 0, 50000,
                mixedBenefitJsonWithLivingTop(), "alpha.png", true));
        cardProductRepository.save(CardProduct.create(
                "Charlie Low", "Issuer", "설명", 0, 50000,
                singleBenefitJson(TRANSPORT_CATEGORY_ID, TRANSPORT_CATEGORY_NAME, 8.0), "charlie.png", true));

        // when
        final CardRecommendationResponse response = cardService.getPreferenceRecommendations(user.getId());

        // then
        assertThat(response.getRecommendations())
                .extracting(card -> card.getCardName())
                .containsExactly("Alpha Dual", "Bravo Single", "Charlie Low");
    }

    @DisplayName("카드 추천 v2는 회원가입 소비분야가 없거나 유효하지 않으면 이름순 fallback을 반환한다")
    @Test
    void getPreferenceRecommendations_withoutValidPaymentType_returnsFallback() {
        // given
        final User user = userRepository.save(User.register(
                Email.of("preference-fallback-user@ssafy.com"),
                "tester",
                "hashed-password"
        ));
        user.updatePaymentType("TRANSFER,UNKNOWN");
        userRepository.flush();

        cardProductRepository.save(CardProduct.create("Foxtrot Card", "Issuer", "설명", 300000, 40000, BENEFITS_JSON, "f.png", true));
        cardProductRepository.save(CardProduct.create("Echo Card", "Issuer", "설명", 300000, 40000, BENEFITS_JSON, "e.png", true));
        cardProductRepository.save(CardProduct.create("Delta Card", "Issuer", "설명", 300000, 40000, BENEFITS_JSON, "d.png", true));
        cardProductRepository.save(CardProduct.create("Charlie Card", "Issuer", "설명", 300000, 40000, BENEFITS_JSON, "c.png", true));
        cardProductRepository.save(CardProduct.create("Bravo Card", "Issuer", "설명", 300000, 40000, BENEFITS_JSON, "b.png", true));
        cardProductRepository.save(CardProduct.create("Alpha Card", "Issuer", "설명", 300000, 40000, BENEFITS_JSON, "a.png", true));

        // when
        final CardRecommendationResponse response = cardService.getPreferenceRecommendations(user.getId());

        // then
        assertThat(response.getRecommendations())
                .extracting(card -> card.getCardName())
                .containsExactly("Alpha Card", "Bravo Card", "Charlie Card", "Delta Card", "Echo Card");
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

    @DisplayName("전체 카드 조회는 object name 이미지를 퍼블릭 URL로 변환한다")
    @Test
    void getCards_convertsObjectNameToPublicUrl() {
        // given
        cardProductRepository.save(CardProduct.create(
                "Alpha Card",
                "Issuer",
                "설명",
                300000,
                40000,
                BENEFITS_JSON,
                "alpha.png",
                true
        ));
        given(imageStorageService.getPublicUrl("alpha.png"))
                .willReturn("https://objectstorage.ap-singapore-1.oraclecloud.com/n/test-namespace/b/card-images/o/alpha.png");

        // when
        final CardListResponse response = cardService.getCards();

        // then
        assertThat(response.getCards()).hasSize(1);
        assertThat(response.getCards().get(0).getCardImageUrl())
                .isEqualTo("https://objectstorage.ap-singapore-1.oraclecloud.com/n/test-namespace/b/card-images/o/alpha.png");
    }

    @DisplayName("전체 카드 조회는 절대 URL 이미지를 그대로 반환한다")
    @Test
    void getCards_keepsDirectImageUrl() {
        // given
        cardProductRepository.save(CardProduct.create(
                "Alpha Card",
                "Issuer",
                "설명",
                300000,
                40000,
                BENEFITS_JSON,
                "https://cdn.example.com/cards/alpha.png",
                true
        ));

        // when
        final CardListResponse response = cardService.getCards();

        // then
        assertThat(response.getCards()).hasSize(1);
        assertThat(response.getCards().get(0).getCardImageUrl())
                .isEqualTo("https://cdn.example.com/cards/alpha.png");
        then(imageStorageService).shouldHaveNoInteractions();
    }

    @DisplayName("카드 추천은 최종 5개 카드에 대해서만 퍼블릭 URL을 생성한다")
    @Test
    void getRecommendations_generatesImageUrlOnlyForTopFiveCards() {
        // given
        final User user = userRepository.save(User.register(
                Email.of("top-five-user@ssafy.com"),
                "tester",
                "hashed-password"
        ));
        memberPaymentHistoryRepository.save(MemberPaymentHistory.create(
                user.getId(), LIVING_CATEGORY_ID, LIVING_CATEGORY_NAME, "스타벅스", 100000, LocalDate.of(2026, 2, 15)));
        given(imageStorageService.getPublicUrl(anyString()))
                .willReturn("https://objectstorage.ap-singapore-1.oraclecloud.com/n/test-namespace/b/card-images/o/card.png");

        cardProductRepository.save(CardProduct.create(
                "Card 1", "Issuer", "설명", 0, 50000, singleBenefitJson(LIVING_CATEGORY_ID, LIVING_CATEGORY_NAME, 1.0), "1.png", true));
        cardProductRepository.save(CardProduct.create(
                "Card 2", "Issuer", "설명", 0, 50000, singleBenefitJson(LIVING_CATEGORY_ID, LIVING_CATEGORY_NAME, 2.0), "2.png", true));
        cardProductRepository.save(CardProduct.create(
                "Card 3", "Issuer", "설명", 0, 50000, singleBenefitJson(LIVING_CATEGORY_ID, LIVING_CATEGORY_NAME, 3.0), "3.png", true));
        cardProductRepository.save(CardProduct.create(
                "Card 4", "Issuer", "설명", 0, 50000, singleBenefitJson(LIVING_CATEGORY_ID, LIVING_CATEGORY_NAME, 4.0), "4.png", true));
        cardProductRepository.save(CardProduct.create(
                "Card 5", "Issuer", "설명", 0, 50000, singleBenefitJson(LIVING_CATEGORY_ID, LIVING_CATEGORY_NAME, 5.0), "5.png", true));
        cardProductRepository.save(CardProduct.create(
                "Card 6", "Issuer", "설명", 0, 50000, singleBenefitJson(LIVING_CATEGORY_ID, LIVING_CATEGORY_NAME, 6.0), "6.png", true));

        // when
        final CardRecommendationResponse response = cardService.getRecommendations(user.getId());

        // then
        assertThat(response.getRecommendations()).hasSize(5);
        assertThat(response.getRecommendations())
                .extracting(card -> card.getCardName())
                .doesNotContain("Card 1");
        then(imageStorageService).should(times(5)).getPublicUrl(anyString());
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

    private String mixedBenefitJsonWithLivingTop() {
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
                    "categoryId": "CG-4fa85f6455cad4a",
                    "categoryName": "교통",
                    "categoryDescription": "",
                    "discountRate": 6.0,
                    "exampleMerchants": ["sample"]
                  }
                ]
                """;
    }
}
