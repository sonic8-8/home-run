package io.ssafy.p.j14c103.homerun.api.controller.card.request;

import io.ssafy.p.j14c103.homerun.api.service.card.request.CardApplyServiceRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CardApplyRequest {

    @NotNull(message = "{validation.card.apply.cardProductId.notNull}")
    private Long cardProductId;

    @Builder
    private CardApplyRequest(final Long cardProductId) {
        this.cardProductId = cardProductId;
    }

    public CardApplyServiceRequest toServiceRequest() {
        return CardApplyServiceRequest.builder()
                .cardProductId(cardProductId)
                .build();
    }
}
