package io.ssafy.p.j14c103.homerun.api.service.card;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ssafy.p.j14c103.homerun.api.service.card.response.CardBenefitResponse;
import io.ssafy.p.j14c103.homerun.api.service.card.response.CardListResponse;
import io.ssafy.p.j14c103.homerun.api.service.card.response.CardRecommendationResponse;
import io.ssafy.p.j14c103.homerun.api.service.card.response.CardResponse;
import io.ssafy.p.j14c103.homerun.api.service.image.ImageStorageService;
import io.ssafy.p.j14c103.homerun.domain.card.CardProduct;
import io.ssafy.p.j14c103.homerun.domain.card.CardProductRepository;
import io.ssafy.p.j14c103.homerun.domain.paymenthistory.MemberPaymentHistory;
import io.ssafy.p.j14c103.homerun.domain.paymenthistory.MemberPaymentHistoryRepository;
import io.ssafy.p.j14c103.homerun.domain.spending.SpendingCategory;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
                    .thenComparing(candidate -> candidate.cardProduct().getCardName());
    private static final Comparator<PreferenceRecommendationCandidate> PREFERENCE_RECOMMENDATION_ORDER =
            Comparator.<PreferenceRecommendationCandidate, BigDecimal>comparing(
                            PreferenceRecommendationCandidate::highestMatchedRate,
                            Comparator.naturalOrder()
                    )
                    .reversed()
                    .thenComparing(Comparator.comparingInt(
                            PreferenceRecommendationCandidate::matchedBenefitCount
                    ).reversed())
                    .thenComparing(candidate -> candidate.cardProduct().getCardName());

    private final CardProductRepository cardProductRepository;
    private final MemberPaymentHistoryRepository memberPaymentHistoryRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final Optional<ImageStorageService> imageStorageService;

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
                .map(this::toCardResponse)
                .toList();
        return CardRecommendationResponse.of(recommendations);
    }

    @Transactional(readOnly = true)
    public CardRecommendationResponse getPreferenceRecommendations(final Long userId) {
        final User user = getRequiredUser(userId);
        final List<SpendingCategory> preferredCategories = SpendingCategory.fromStoredCodes(user.getPaymentType());
        if (preferredCategories.isEmpty()) {
            return buildFallbackRecommendations();
        }

        final Set<SpendingCategory> preferredCategorySet = EnumSet.copyOf(preferredCategories);
        final List<CardResponse> recommendations = cardProductRepository.findByActiveYnTrueOrderByCardNameAsc().stream()
                .map(cardProduct -> toPreferenceRecommendationCandidate(cardProduct, preferredCategorySet))
                .filter(candidate -> candidate.highestMatchedRate().signum() > 0)
                .sorted(PREFERENCE_RECOMMENDATION_ORDER)
                .limit(5)
                .map(this::toCardResponse)
                .toList();
        if (recommendations.isEmpty()) {
            return buildFallbackRecommendations();
        }

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

    private User getRequiredUser(final Long userId) {
        if (userId == null) {
            throw new HomerunException(ErrorCode.USER_ID_REQUIRED);
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new HomerunException(ErrorCode.USER_NOT_FOUND));
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
        final List<CardBenefitResponse> benefits = parseBenefits(cardProduct.getActiveBenefits());
        final SavingAmounts savingAmounts = calculateSaving(
                benefits,
                spendByCategoryId,
                cardProduct.getMaxBenefitLimitAmount()
        );

        return new CardRecommendationCandidate(
                cardProduct,
                benefits,
                savingAmounts.rawSaving(),
                savingAmounts.estimatedSaving()
        );
    }

    private PreferenceRecommendationCandidate toPreferenceRecommendationCandidate(
            final CardProduct cardProduct,
            final Set<SpendingCategory> preferredCategories
    ) {
        final List<CardBenefitResponse> benefits = parseBenefits(cardProduct.getActiveBenefits());
        final PreferenceRecommendationScore score = calculatePreferenceScore(benefits, preferredCategories);

        return new PreferenceRecommendationCandidate(
                cardProduct,
                benefits,
                score.highestMatchedRate(),
                score.matchedBenefitCount()
        );
    }

    private CardResponse toCardResponse(final CardProduct cardProduct) {
        final List<CardBenefitResponse> benefits = parseBenefits(cardProduct.getActiveBenefits());
        return toCardResponse(cardProduct, benefits);
    }

    private CardResponse toCardResponse(final CardRecommendationCandidate candidate) {
        return toCardResponse(candidate.cardProduct(), candidate.benefits());
    }

    private CardResponse toCardResponse(final PreferenceRecommendationCandidate candidate) {
        return toCardResponse(candidate.cardProduct(), candidate.benefits());
    }

    private CardResponse toCardResponse(
            final CardProduct cardProduct,
            final List<CardBenefitResponse> benefits
    ) {
        return CardResponse.of(
                cardProduct.getId(),
                cardProduct.getCardName(),
                cardProduct.getCardIssuerName(),
                cardProduct.getCardDescription(),
                cardProduct.getBaselinePerformanceAmount(),
                cardProduct.getMaxBenefitLimitAmount(),
                resolveCardImageUrl(cardProduct.getCardImageUrl()),
                benefits
        );
    }

    private String resolveCardImageUrl(final String cardImageUrl) {
        if (cardImageUrl == null || cardImageUrl.isBlank()) {
            return "";
        }
        if (isDirectImageUrl(cardImageUrl)) {
            return cardImageUrl;
        }

        final ImageStorageService service = imageStorageService.orElse(null);
        if (service == null) {
            return "";
        }

        final String publicUrl = service.getPublicUrl(cardImageUrl);
        if (publicUrl == null || publicUrl.isBlank()) {
            return "";
        }

        return publicUrl;
    }

    private boolean isDirectImageUrl(final String cardImageUrl) {
        if (cardImageUrl.startsWith("http://")) {
            return true;
        }
        if (cardImageUrl.startsWith("https://")) {
            return true;
        }
        return cardImageUrl.startsWith("data:");
    }

    private SavingAmounts calculateSaving(
            final List<CardBenefitResponse> benefits,
            final Map<String, BigDecimal> spendByCategoryId,
            final Long maxBenefitLimitAmount
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

    private PreferenceRecommendationScore calculatePreferenceScore(
            final List<CardBenefitResponse> benefits,
            final Set<SpendingCategory> preferredCategories
    ) {
        BigDecimal highestMatchedRate = BigDecimal.ZERO;
        int matchedBenefitCount = 0;

        for (final CardBenefitResponse benefit : benefits) {
            final SpendingCategory category = SpendingCategory.from(benefit.getCategoryName());
            if (!preferredCategories.contains(category)) {
                continue;
            }

            matchedBenefitCount++;
            if (benefit.getDiscountRate().compareTo(highestMatchedRate) > 0) {
                highestMatchedRate = benefit.getDiscountRate();
            }
        }

        return new PreferenceRecommendationScore(highestMatchedRate, matchedBenefitCount);
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
            CardProduct cardProduct,
            List<CardBenefitResponse> benefits,
            BigDecimal rawSaving,
            BigDecimal estimatedSaving
    ) {
    }

    private record PreferenceRecommendationCandidate(
            CardProduct cardProduct,
            List<CardBenefitResponse> benefits,
            BigDecimal highestMatchedRate,
            int matchedBenefitCount
    ) {
    }

    private record SavingAmounts(
            BigDecimal rawSaving,
            BigDecimal estimatedSaving
    ) {
    }

    private record PreferenceRecommendationScore(
            BigDecimal highestMatchedRate,
            int matchedBenefitCount
    ) {
    }
}
