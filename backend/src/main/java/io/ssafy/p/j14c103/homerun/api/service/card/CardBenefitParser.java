package io.ssafy.p.j14c103.homerun.api.service.card;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ssafy.p.j14c103.homerun.api.service.card.response.CardBenefitResponse;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardBenefitParser {

    private final ObjectMapper objectMapper;

    public List<CardBenefitResponse> parse(final String activeBenefits) {
        if (activeBenefits == null || activeBenefits.isBlank()) {
            return List.of();
        }

        try {
            final List<Map<String, Object>> payloads = objectMapper.readValue(
                    activeBenefits,
                    new TypeReference<>() {
                    }
            );
            return toResponses(payloads);
        } catch (final JsonProcessingException exception) {
            throw new HomerunException(ErrorCode.GLOBAL_SERIALIZATION_ERROR, exception);
        }
    }

    private List<CardBenefitResponse> toResponses(final List<Map<String, Object>> payloads) {
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
