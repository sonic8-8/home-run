package io.ssafy.p.j14c103.homerun.api.controller.game.career.request;

import io.ssafy.p.j14c103.homerun.api.service.game.career.request.AcceptJobTransferServiceRequest;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AcceptJobTransferRequest {

    private String offerId;

    @Builder(access = AccessLevel.PRIVATE)
    private AcceptJobTransferRequest(final String offerId) {
        this.offerId = offerId;
    }

    public static AcceptJobTransferRequest of(final String offerId) {
        return AcceptJobTransferRequest.builder()
            .offerId(offerId)
            .build();
    }

    public AcceptJobTransferServiceRequest toServiceRequest() {
        return AcceptJobTransferServiceRequest.of(offerId);
    }
}
