package io.ssafy.p.j14c103.homerun.api.service.card;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ssafy.p.j14c103.homerun.api.service.card.response.CardBenefitResponse;
import io.ssafy.p.j14c103.homerun.api.service.card.response.CardListResponse;
import io.ssafy.p.j14c103.homerun.api.service.card.response.CardRecommendationResponse;
import io.ssafy.p.j14c103.homerun.api.service.card.response.CardResponse;
import io.ssafy.p.j14c103.homerun.domain.card.CardProduct;
import io.ssafy.p.j14c103.homerun.domain.card.CardProductRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardProductRepository cardProductRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public CardListResponse getCards() {
        final List<CardResponse> cards = cardProductRepository.findByActiveYnTrueOrderByCardNameAsc().stream()
                .map(this::toCardResponse)
                .toList();
        return CardListResponse.of(cards);
    }

    @Transactional(readOnly = true)
    public CardRecommendationResponse getRecommendations(final Long userId) {
        validateUser(userId);

        final List<CardResponse> recommendations = cardProductRepository.findTop5ByActiveYnTrueOrderByCardNameAsc()
                .stream()
                .map(this::toCardResponse)
                .toList();
        return CardRecommendationResponse.of(recommendations);
    }

    private void validateUser(final Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (userRepository.existsById(userId)) {
            return;
        }

        throw new HomerunException(ErrorCode.USER_NOT_FOUND);
    }

    private CardResponse toCardResponse(final CardProduct cardProduct) {
        return CardResponse.of(
                cardProduct.getId(),
                cardProduct.getCardName(),
                cardProduct.getCardIssuerName(),
                cardProduct.getCardDescription(),
                cardProduct.getBaselinePerformanceAmount(),
                cardProduct.getMaxBenefitLimitAmount(),
                cardProduct.getCardImageUrl(),
                parseBenefits(cardProduct.getActiveBenefits())
        );
    }

    private List<CardBenefitResponse> parseBenefits(final String activeBenefits) {
        if (activeBenefits == null || activeBenefits.isBlank()) {
            return List.of();
        }

        try {
            final List<Map<String, Object>> payloads = objectMapper.readValue(
                    activeBenefits,
                    new TypeReference<List<Map<String, Object>>>() {
                    }
            );
            final List<CardBenefitResponse> responses = new ArrayList<>();

            for (final Map<String, Object> payload : payloads) {
                responses.add(CardBenefitResponse.of(
                        toStringValue(payload.get("categoryId")),
                        toStringValue(payload.get("categoryName")),
                        toStringValue(payload.get("categoryDescription")),
                        toBigDecimal(payload.get("discountRate")),
                        toStringList(payload.get("exampleMerchants"))
                ));
            }
            return responses;
        } catch (final JsonProcessingException exception) {
            throw new HomerunException(ErrorCode.GLOBAL_SERIALIZATION_ERROR, exception);
        }
    }

    private String toStringValue(final Object value) {
        if (value == null) {
            return "";
        }
        return value.toString();
    }

    private BigDecimal toBigDecimal(final Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.toString());
    }

    private List<String> toStringList(final Object value) {
        if (!(value instanceof List<?> values)) {
            return List.of();
        }

        return values.stream()
                .map(String::valueOf)
                .toList();
    }
}
