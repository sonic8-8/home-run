package io.ssafy.p.j14c103.homerun.api.service.card;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.card.response.CardListResponse;
import io.ssafy.p.j14c103.homerun.api.service.card.response.CardRecommendationResponse;
import io.ssafy.p.j14c103.homerun.domain.card.CardProduct;
import io.ssafy.p.j14c103.homerun.domain.card.CardProductRepository;
import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import jakarta.transaction.Transactional;
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
                "categoryId": "CG-001",
                "categoryName": "생활",
                "categoryDescription": "생활 업종",
                "discountRate": 10.0,
                "exampleMerchants": ["스타벅스"]
              }
            ]
            """;

    @Autowired
    private CardService cardService;

    @Autowired
    private CardProductRepository cardProductRepository;

    @Autowired
    private UserRepository userRepository;

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
        assertThat(response.getCards().get(0).getActiveBenefits()).hasSize(1);
        assertThat(response.getCards().get(0).getActiveBenefits().get(0).getCategoryName()).isEqualTo("생활");
    }

    @DisplayName("카드 추천은 사용자 결제 유형이 없어도 이름순 상위 5건을 반환한다")
    @Test
    void getRecommendations() {
        // given
        final User user = userRepository.save(User.register(
                Email.of("card-user@ssafy.com"),
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
}
