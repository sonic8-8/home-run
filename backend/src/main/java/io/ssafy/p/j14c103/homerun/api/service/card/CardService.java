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
import io.ssafy.p.j14c103.homerun.domain.paymenthistory.MemberPaymentHistory;
import io.ssafy.p.j14c103.homerun.domain.paymenthistory.MemberPaymentHistoryRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CardService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final Comparator<CardRecommendationCandidate> RECOMMENDATION_ORDER =
            Comparator.<CardRecommendationCandidate, BigDecimal>comparing(
                            CardRecommendationCandidate::estimatedSaving,
                            Comparator.naturalOrder()
                    )
                    .reversed()
                    .thenComparing(Comparator.comparing(
                            CardRecommendationCandidate::rawSaving,
                            Comparator.naturalOrder()
                    ).reversed())
                    .thenComparing(candidate -> candidate.cardResponse().getCardName());

    private final CardProductRepository cardProductRepository;
    private final MemberPaymentHistoryRepository memberPaymentHistoryRepository;
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

        final List<MemberPaymentHistory> paymentHistories = memberPaymentHistoryRepository
                .findAllByUserIdOrderByPaymentDateDescCreatedAtDesc(userId);
        if (paymentHistories.isEmpty()) {
            return buildFallbackRecommendations();
        }

        final List<MemberPaymentHistory> latestMonthHistories = filterLatestMonth(paymentHistories);
        final Map<String, BigDecimal> spendByCategoryId = latestMonthHistories.stream()
                .collect(Collectors.groupingBy(
                        MemberPaymentHistory::getCategoryId,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                history -> BigDecimal.valueOf(history.getPaymentAmount()),
                                BigDecimal::add
                        )
                ));

        final List<CardResponse> recommendations = cardProductRepository.findByActiveYnTrueOrderByCardNameAsc().stream()
                .map(cardProduct -> toRecommendationCandidate(cardProduct, spendByCategoryId))
                .sorted(RECOMMENDATION_ORDER)
                .limit(5)
                .map(CardRecommendationCandidate::cardResponse)
                .toList();
        return CardRecommendationResponse.of(recommendations);
    }

    private CardRecommendationResponse buildFallbackRecommendations() {
        final List<CardResponse> recommendations = cardProductRepository.findTop5ByActiveYnTrueOrderByCardNameAsc()
                .stream()
                .map(this::toCardResponse)
                .toList();
        return CardRecommendationResponse.of(recommendations);
    }

    private List<MemberPaymentHistory> filterLatestMonth(final List<MemberPaymentHistory> paymentHistories) {
        final MemberPaymentHistory latestHistory = paymentHistories.get(0);
        final int latestYear = latestHistory.getPaymentDate().getYear();
        final int latestMonth = latestHistory.getPaymentDate().getMonthValue();

        return paymentHistories.stream()
                .filter(history -> history.getPaymentDate().getYear() == latestYear)
                .filter(history -> history.getPaymentDate().getMonthValue() == latestMonth)
                .toList();
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

    private CardRecommendationCandidate toRecommendationCandidate(
            final CardProduct cardProduct,
            final Map<String, BigDecimal> spendByCategoryId
    ) {
        final CardResponse cardResponse = toCardResponse(cardProduct);
        final SavingAmounts savingAmounts = calculateSaving(
                cardResponse.getActiveBenefits(),
                spendByCategoryId,
                cardProduct.getMaxBenefitLimitAmount()
        );

        return new CardRecommendationCandidate(
                cardResponse,
                savingAmounts.rawSaving(),
                savingAmounts.estimatedSaving()
        );
    }

    private CardResponse toCardResponse(final CardProduct cardProduct) {
        final List<CardBenefitResponse> benefits = parseBenefits(cardProduct.getActiveBenefits());

        return CardResponse.of(
                cardProduct.getId(),
                cardProduct.getCardName(),
                cardProduct.getCardIssuerName(),
                cardProduct.getCardDescription(),
                cardProduct.getBaselinePerformanceAmount(),
                cardProduct.getMaxBenefitLimitAmount(),
                cardProduct.getCardImageUrl(),
                benefits
        );
    }

    private SavingAmounts calculateSaving(
            final List<CardBenefitResponse> benefits,
            final Map<String, BigDecimal> spendByCategoryId,
            final Integer maxBenefitLimitAmount
    ) {
        BigDecimal rawSaving = BigDecimal.ZERO;
        for (final CardBenefitResponse benefit : benefits) {
            final BigDecimal categorySpend = spendByCategoryId.getOrDefault(benefit.getCategoryId(), BigDecimal.ZERO);
            if (categorySpend.signum() == 0) {
                continue;
            }

            rawSaving = rawSaving.add(
                    categorySpend.multiply(benefit.getDiscountRate()).divide(HUNDRED, 2, RoundingMode.DOWN)
            );
        }

        final BigDecimal estimatedSaving;
        if (rawSaving.signum() <= 0) {
            estimatedSaving = BigDecimal.ZERO;
        } else if (maxBenefitLimitAmount == null || maxBenefitLimitAmount <= 0) {
            estimatedSaving = rawSaving;
        } else {
            estimatedSaving = rawSaving.min(BigDecimal.valueOf(maxBenefitLimitAmount));
        }

        return new SavingAmounts(rawSaving, estimatedSaving);
    }

    private List<CardBenefitResponse> parseBenefits(final String activeBenefits) {
        if (activeBenefits == null || activeBenefits.isBlank()) {
            return List.of();
        }

        try {
            final List<Map<String, Object>> payloads = objectMapper.readValue(
                    activeBenefits,
                    new TypeReference<>() {
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

    private record CardRecommendationCandidate(
            CardResponse cardResponse,
            BigDecimal rawSaving,
            BigDecimal estimatedSaving
    ) {
    }

    private record SavingAmounts(
            BigDecimal rawSaving,
            BigDecimal estimatedSaving
    ) {
    }
}
