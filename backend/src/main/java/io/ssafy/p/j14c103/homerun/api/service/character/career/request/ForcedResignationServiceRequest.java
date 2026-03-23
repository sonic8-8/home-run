package io.ssafy.p.j14c103.homerun.api.service.character.career.request;

import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareer;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;

public record ForcedResignationServiceRequest(
    GameCareer gameCareer,
    GameStat gameStat,
    int currentTurn
) {

    public ForcedResignationServiceRequest {
        if (gameCareer == null || gameStat == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
        if (currentTurn < 1) {
            throw new HomerunException(ErrorCode.CHARACTER_TURN_INVALID);
        }
    }

    public static ForcedResignationServiceRequest of(
        final GameCareer gameCareer,
        final GameStat gameStat,
        final int currentTurn
    ) {
        return new ForcedResignationServiceRequest(gameCareer, gameStat, currentTurn);
    }
}
