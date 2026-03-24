package io.ssafy.p.j14c103.homerun.api.service.character.career.request;

import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareer;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;

public record UnemploymentBenefitServiceRequest(GameCareer gameCareer) {

    public UnemploymentBenefitServiceRequest {
        if (gameCareer == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
    }

    public static UnemploymentBenefitServiceRequest of(final GameCareer gameCareer) {
        return new UnemploymentBenefitServiceRequest(gameCareer);
    }
}
