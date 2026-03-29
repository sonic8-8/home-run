package io.ssafy.p.j14c103.homerun.api.service.card;

import io.ssafy.p.j14c103.homerun.api.service.card.request.CardApplyServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.card.response.CardBenefitResponse;
import io.ssafy.p.j14c103.homerun.api.service.card.response.OwnedCardListResponse;
import io.ssafy.p.j14c103.homerun.api.service.card.response.OwnedCardResponse;
import io.ssafy.p.j14c103.homerun.domain.account.AccountType;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountRepository;
import io.ssafy.p.j14c103.homerun.domain.card.CardProduct;
import io.ssafy.p.j14c103.homerun.domain.card.CardProductRepository;
import io.ssafy.p.j14c103.homerun.domain.card.OwnedCard;
import io.ssafy.p.j14c103.homerun.domain.card.OwnedCardRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OwnedCardService {

    private final CardProductRepository cardProductRepository;
    private final OwnedCardRepository ownedCardRepository;
    private final UserAccountRepository userAccountRepository;
    private final UserRepository userRepository;
    private final CardBenefitParser cardBenefitParser;
    private final CardImageUrlResolver cardImageUrlResolver;

    @Transactional(readOnly = true)
    public OwnedCardListResponse getOwnedCards(final Long userId) {
        validateUser(userId);

        final List<OwnedCardResponse> cards = ownedCardRepository.findAllByUserIdAndActiveYnTrueOrderByOpenedAtDesc(userId)
                .stream()
                .map(this::toOwnedCardResponse)
                .toList();

        return OwnedCardListResponse.of(cards);
    }

    @Transactional
    public OwnedCardResponse applyCard(final Long userId, final CardApplyServiceRequest request) {
        validateUser(userId);
        validateMainAccount(userId);

        final CardProduct cardProduct = getActiveCardProduct(request.getCardProductId());
        if (ownedCardRepository.existsByUserIdAndCardProductIdAndActiveYnTrue(userId, cardProduct.getId())) {
            throw new HomerunException(ErrorCode.CARD_ALREADY_OWNED);
        }

        final OwnedCard ownedCard = ownedCardRepository.save(OwnedCard.create(
                userId,
                cardProduct,
                defaultCardAlias(cardProduct),
                maskCardNumber(userId, ownedCardRepository.countByUserId(userId)),
                LocalDateTime.now()
        ));

        return toOwnedCardResponse(ownedCard);
    }

    @Transactional
    public void cancelOwnedCard(final Long userId, final Long ownedCardId) {
        validateUser(userId);

        final OwnedCard ownedCard = ownedCardRepository.findByIdAndUserId(ownedCardId, userId)
                .orElseThrow(() -> new HomerunException(ErrorCode.CARD_OWNED_NOT_FOUND));
        if (!ownedCard.isActive()) {
            throw new HomerunException(ErrorCode.CARD_ALREADY_CANCELED);
        }

        ownedCard.cancel();
    }

    private void validateUser(final Long userId) {
        if (userId == null) {
            throw new HomerunException(ErrorCode.USER_ID_REQUIRED);
        }
        if (userRepository.existsById(userId)) {
            return;
        }

        throw new HomerunException(ErrorCode.USER_NOT_FOUND);
    }

    private void validateMainAccount(final Long userId) {
        if (userAccountRepository.findByUserIdAndAccountType(userId, AccountType.MAIN).isPresent()) {
            return;
        }

        throw new HomerunException(ErrorCode.USER_ASSET_LINK_REQUIRED);
    }

    private CardProduct getActiveCardProduct(final Long cardProductId) {
        if (cardProductId == null) {
            throw new HomerunException(ErrorCode.CARD_PRODUCT_NOT_FOUND);
        }

        return cardProductRepository.findById(cardProductId)
                .filter(CardProduct::isActiveYn)
                .orElseThrow(() -> new HomerunException(ErrorCode.CARD_PRODUCT_NOT_FOUND));
    }

    private OwnedCardResponse toOwnedCardResponse(final OwnedCard ownedCard) {
        final CardProduct cardProduct = ownedCard.getCardProduct();
        final List<CardBenefitResponse> benefits = cardBenefitParser.parse(cardProduct.getActiveBenefits());

        return OwnedCardResponse.of(
                ownedCard.getId(),
                cardProduct.getId(),
                cardProduct.getCardName(),
                cardProduct.getCardIssuerName(),
                cardProduct.getCardDescription(),
                cardProduct.getBaselinePerformanceAmount(),
                cardProduct.getMaxBenefitLimitAmount(),
                benefits,
                ownedCard.getCardAlias(),
                ownedCard.getMaskedCardNo(),
                cardImageUrlResolver.resolve(cardProduct.getCardImageUrl()),
                ownedCard.getOpenedAt()
        );
    }

    private String defaultCardAlias(final CardProduct cardProduct) {
        return cardProduct.getCardName();
    }

    private String maskCardNumber(final Long userId, final long cardSequence) {
        final String suffix = String.format("%04d", Math.floorMod(userId * 10L + cardSequence, 10_000L));
        return "1234-****-****-" + suffix;
    }
}
