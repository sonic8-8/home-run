package io.ssafy.p.j14c103.homerun.api.service.card;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import io.ssafy.p.j14c103.homerun.api.service.card.request.CardApplyServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.card.response.OwnedCardListResponse;
import io.ssafy.p.j14c103.homerun.api.service.card.response.OwnedCardResponse;
import io.ssafy.p.j14c103.homerun.api.service.image.ImageStorageService;
import io.ssafy.p.j14c103.homerun.domain.account.AccountType;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccount;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountRepository;
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
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OwnedCardServiceTest {

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

    @Autowired
    private OwnedCardService ownedCardService;

    @Autowired
    private CardProductRepository cardProductRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private OwnedCardRepository ownedCardRepository;

    @MockitoBean
    private ImageStorageService imageStorageService;

    @DisplayName("보유 카드 조회는 활성 카드만 최신 개설순으로 반환한다")
    @Test
    void getOwnedCards() {
        // given
        final User user = userRepository.save(User.register(
                Email.of("owned-card-user@ssafy.com"),
                "tester",
                "hashed-password"
        ));
        final CardProduct alphaCard = cardProductRepository.save(CardProduct.create(
                "Alpha Card", "Issuer", "설명", 300000, 40000, BENEFITS_JSON, "alpha.png", true));
        final CardProduct bravoCard = cardProductRepository.save(CardProduct.create(
                "Bravo Card", "Issuer", "설명", 300000, 40000, BENEFITS_JSON, "bravo.png", true));
        ownedCardRepository.save(OwnedCard.create(
                user.getId(),
                alphaCard,
                "Alpha 메인",
                "1234-****-****-0001",
                LocalDateTime.of(2026, 1, 1, 0, 0)
        ));
        ownedCardRepository.save(OwnedCard.create(
                user.getId(),
                bravoCard,
                "Bravo 메인",
                "1234-****-****-0002",
                LocalDateTime.of(2026, 3, 1, 0, 0)
        ));

        // when
        final OwnedCardListResponse response = ownedCardService.getOwnedCards(user.getId());

        // then
        assertThat(response.getCards()).hasSize(2);
        assertThat(response.getCards())
                .extracting(OwnedCardResponse::getCardName)
                .containsExactly("Bravo Card", "Alpha Card");
        assertThat(response.getCards().get(0).getActiveBenefits()).hasSize(3);
    }

    @DisplayName("보유 카드 조회는 object name 이미지를 퍼블릭 URL로 변환한다")
    @Test
    void getOwnedCards_convertsObjectNameToPublicUrl() {
        // given
        final User user = userRepository.save(User.register(
                Email.of("owned-card-image-user@ssafy.com"),
                "tester",
                "hashed-password"
        ));
        final CardProduct cardProduct = cardProductRepository.save(CardProduct.create(
                "Alpha Card", "Issuer", "설명", 300000, 40000, BENEFITS_JSON, "alpha.png", true));
        ownedCardRepository.save(OwnedCard.create(
                user.getId(),
                cardProduct,
                "Alpha 메인",
                "1234-****-****-0001",
                LocalDateTime.of(2026, 3, 1, 0, 0)
        ));
        given(imageStorageService.getPublicUrl("alpha.png"))
                .willReturn("https://objectstorage.ap-singapore-1.oraclecloud.com/n/test-namespace/b/card-images/o/alpha.png");

        // when
        final OwnedCardListResponse response = ownedCardService.getOwnedCards(user.getId());

        // then
        assertThat(response.getCards()).hasSize(1);
        assertThat(response.getCards().get(0).getCardImageUrl())
                .isEqualTo("https://objectstorage.ap-singapore-1.oraclecloud.com/n/test-namespace/b/card-images/o/alpha.png");
    }

    @DisplayName("카드 신청은 보유 카드로 저장하고 응답을 반환한다")
    @Test
    void applyCard() {
        // given
        final User user = userRepository.save(User.register(
                Email.of("apply-card-user@ssafy.com"),
                "tester",
                "hashed-password"
        ));
        createMainAccount(user.getId());
        final CardProduct cardProduct = cardProductRepository.save(CardProduct.create(
                "Alpha Card", "Issuer", "설명", 300000, 40000, BENEFITS_JSON, "alpha.png", true));

        // when
        final OwnedCardResponse response = ownedCardService.applyCard(
                user.getId(),
                CardApplyServiceRequest.builder()
                        .cardProductId(cardProduct.getId())
                        .build()
        );

        // then
        assertThat(response.getOwnedCardId()).isNotNull();
        assertThat(response.getCardProductId()).isEqualTo(cardProduct.getId());
        assertThat(response.getCardAlias()).isEqualTo("Alpha Card");
        assertThat(response.getMaskedCardNo())
                .isEqualTo("1234-****-****-" + String.format("%04d", (user.getId() * 10L) % 10_000L));
        assertThat(ownedCardRepository.findAllByUserIdAndActiveYnTrueOrderByOpenedAtDesc(user.getId())).hasSize(1);
    }

    @DisplayName("카드 신청은 같은 카드의 중복 보유를 허용하지 않는다")
    @Test
    void applyCard_rejectsDuplicateOwnedCard() {
        // given
        final User user = userRepository.save(User.register(
                Email.of("duplicate-card-user@ssafy.com"),
                "tester",
                "hashed-password"
        ));
        createMainAccount(user.getId());
        final CardProduct cardProduct = cardProductRepository.save(CardProduct.create(
                "Alpha Card", "Issuer", "설명", 300000, 40000, BENEFITS_JSON, "alpha.png", true));
        ownedCardRepository.save(OwnedCard.create(
                user.getId(),
                cardProduct,
                "Alpha Card",
                "1234-****-****-0010",
                LocalDateTime.now().minusDays(1)
        ));

        // when & then
        assertThatThrownBy(() -> ownedCardService.applyCard(
                user.getId(),
                CardApplyServiceRequest.builder()
                        .cardProductId(cardProduct.getId())
                        .build()
        ))
                .isInstanceOf(HomerunException.class)
                .extracting(exception -> ((HomerunException) exception).getErrorCode())
                .isEqualTo(ErrorCode.CARD_ALREADY_OWNED);
    }

    @DisplayName("카드 신청은 주계좌가 없으면 실패한다")
    @Test
    void applyCard_requiresMainAccount() {
        // given
        final User user = userRepository.save(User.register(
                Email.of("missing-account-user@ssafy.com"),
                "tester",
                "hashed-password"
        ));
        final CardProduct cardProduct = cardProductRepository.save(CardProduct.create(
                "Alpha Card", "Issuer", "설명", 300000, 40000, BENEFITS_JSON, "alpha.png", true));

        // when & then
        assertThatThrownBy(() -> ownedCardService.applyCard(
                user.getId(),
                CardApplyServiceRequest.builder()
                        .cardProductId(cardProduct.getId())
                        .build()
        ))
                .isInstanceOf(HomerunException.class)
                .extracting(exception -> ((HomerunException) exception).getErrorCode())
                .isEqualTo(ErrorCode.USER_ASSET_LINK_REQUIRED);
    }

    @DisplayName("카드 신청은 존재하지 않는 카드 상품이면 실패한다")
    @Test
    void applyCard_cardProductNotFound() {
        // given
        final User user = userRepository.save(User.register(
                Email.of("missing-product-user@ssafy.com"),
                "tester",
                "hashed-password"
        ));
        createMainAccount(user.getId());

        // when & then
        assertThatThrownBy(() -> ownedCardService.applyCard(
                user.getId(),
                CardApplyServiceRequest.builder()
                        .cardProductId(999L)
                        .build()
        ))
                .isInstanceOf(HomerunException.class)
                .extracting(exception -> ((HomerunException) exception).getErrorCode())
                .isEqualTo(ErrorCode.CARD_PRODUCT_NOT_FOUND);
    }

    @DisplayName("카드 해지는 active 상태를 비활성으로 변경한다")
    @Test
    void cancelOwnedCard() {
        // given
        final User user = userRepository.save(User.register(
                Email.of("cancel-card-user@ssafy.com"),
                "tester",
                "hashed-password"
        ));
        final CardProduct cardProduct = cardProductRepository.save(CardProduct.create(
                "Alpha Card", "Issuer", "설명", 300000, 40000, BENEFITS_JSON, "alpha.png", true));
        final OwnedCard ownedCard = ownedCardRepository.save(OwnedCard.create(
                user.getId(),
                cardProduct,
                "Alpha Card",
                "1234-****-****-0010",
                LocalDateTime.now()
        ));

        // when
        ownedCardService.cancelOwnedCard(user.getId(), ownedCard.getId());

        // then
        assertThat(ownedCardRepository.findAllByUserIdAndActiveYnTrueOrderByOpenedAtDesc(user.getId())).isEmpty();
    }

    @DisplayName("카드 해지는 이미 해지된 카드면 실패한다")
    @Test
    void cancelOwnedCard_alreadyCanceled() {
        // given
        final User user = userRepository.save(User.register(
                Email.of("already-canceled-user@ssafy.com"),
                "tester",
                "hashed-password"
        ));
        final CardProduct cardProduct = cardProductRepository.save(CardProduct.create(
                "Alpha Card", "Issuer", "설명", 300000, 40000, BENEFITS_JSON, "alpha.png", true));
        final OwnedCard ownedCard = ownedCardRepository.save(OwnedCard.create(
                user.getId(),
                cardProduct,
                "Alpha Card",
                "1234-****-****-0010",
                LocalDateTime.now()
        ));
        ownedCard.cancel();
        ownedCardRepository.flush();

        // when & then
        assertThatThrownBy(() -> ownedCardService.cancelOwnedCard(user.getId(), ownedCard.getId()))
                .isInstanceOf(HomerunException.class)
                .extracting(exception -> ((HomerunException) exception).getErrorCode())
                .isEqualTo(ErrorCode.CARD_ALREADY_CANCELED);
    }

    @DisplayName("카드 해지는 본인 카드만 가능하다")
    @Test
    void cancelOwnedCard_rejectsOtherUsersCard() {
        // given
        final User owner = userRepository.save(User.register(
                Email.of("owner-user@ssafy.com"),
                "owner",
                "hashed-password"
        ));
        final User otherUser = userRepository.save(User.register(
                Email.of("other-user@ssafy.com"),
                "other",
                "hashed-password"
        ));
        final CardProduct cardProduct = cardProductRepository.save(CardProduct.create(
                "Alpha Card", "Issuer", "설명", 300000, 40000, BENEFITS_JSON, "alpha.png", true));
        final OwnedCard ownedCard = ownedCardRepository.save(OwnedCard.create(
                owner.getId(),
                cardProduct,
                "Alpha Card",
                "1234-****-****-0010",
                LocalDateTime.now()
        ));

        // when & then
        assertThatThrownBy(() -> ownedCardService.cancelOwnedCard(otherUser.getId(), ownedCard.getId()))
                .isInstanceOf(HomerunException.class)
                .extracting(exception -> ((HomerunException) exception).getErrorCode())
                .isEqualTo(ErrorCode.CARD_OWNED_NOT_FOUND);
    }

    private UserAccount createMainAccount(final Long userId) {
        return userAccountRepository.save(UserAccount.create(
                userId,
                AccountType.MAIN,
                "001",
                "한국은행",
                "1234567890123456",
                1_000_000L
        ));
    }
}
