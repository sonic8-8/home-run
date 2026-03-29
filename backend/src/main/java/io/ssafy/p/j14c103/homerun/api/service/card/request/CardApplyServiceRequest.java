package io.ssafy.p.j14c103.homerun.api.service.card.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CardApplyServiceRequest {

    private Long cardProductId;

    @Builder
    private CardApplyServiceRequest(final Long cardProductId) {
        this.cardProductId = cardProductId;
    }
}
