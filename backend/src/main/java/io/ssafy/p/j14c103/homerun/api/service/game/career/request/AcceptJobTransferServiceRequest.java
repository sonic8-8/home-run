package io.ssafy.p.j14c103.homerun.api.service.game.career.request;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AcceptJobTransferServiceRequest {

    private String offerId;

    @Builder(access = AccessLevel.PRIVATE)
    private AcceptJobTransferServiceRequest(final String offerId) {
        validateOfferId(offerId);
        this.offerId = offerId;
    }

    public static AcceptJobTransferServiceRequest of(final String offerId) {
        return AcceptJobTransferServiceRequest.builder()
            .offerId(offerId)
            .build();
    }

    private void validateOfferId(final String offerId) {
        if (offerId == null || offerId.isBlank()) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
