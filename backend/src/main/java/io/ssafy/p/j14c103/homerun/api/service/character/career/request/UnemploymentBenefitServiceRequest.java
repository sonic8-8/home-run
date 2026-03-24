package io.ssafy.p.j14c103.homerun.api.service.character.career.request;

import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareer;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UnemploymentBenefitServiceRequest {

    private GameCareer gameCareer;

    @Builder(access = AccessLevel.PRIVATE)
    private UnemploymentBenefitServiceRequest(final GameCareer gameCareer) {
        validateRequest(gameCareer);

        this.gameCareer = gameCareer;
    }

    public static UnemploymentBenefitServiceRequest of(final GameCareer gameCareer) {
        return UnemploymentBenefitServiceRequest.builder()
            .gameCareer(gameCareer)
            .build();
    }

    private void validateRequest(final GameCareer gameCareer) {
        if (gameCareer == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
    }
}
