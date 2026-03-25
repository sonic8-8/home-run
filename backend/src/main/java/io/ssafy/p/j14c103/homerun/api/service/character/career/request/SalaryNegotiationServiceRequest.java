package io.ssafy.p.j14c103.homerun.api.service.character.career.request;

import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareer;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SalaryNegotiationServiceRequest {

    private GameCareer gameCareer;
    private GameStat gameStat;
    private int currentTurn;

    @Builder(access = AccessLevel.PRIVATE)
    private SalaryNegotiationServiceRequest(
        final GameCareer gameCareer,
        final GameStat gameStat,
        final int currentTurn
    ) {
        validateRequest(gameCareer, gameStat, currentTurn);

        this.gameCareer = gameCareer;
        this.gameStat = gameStat;
        this.currentTurn = currentTurn;
    }

    public static SalaryNegotiationServiceRequest of(
        final GameCareer gameCareer,
        final GameStat gameStat,
        final int currentTurn
    ) {
        return SalaryNegotiationServiceRequest.builder()
            .gameCareer(gameCareer)
            .gameStat(gameStat)
            .currentTurn(currentTurn)
            .build();
    }

    public GameCareer gameCareer() {
        return gameCareer;
    }

    public GameStat gameStat() {
        return gameStat;
    }

    public int currentTurn() {
        return currentTurn;
    }

    private void validateRequest(
        final GameCareer gameCareer,
        final GameStat gameStat,
        final int currentTurn
    ) {
        if (gameCareer == null || gameStat == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
        if (currentTurn < 1) {
            throw new HomerunException(ErrorCode.CHARACTER_TURN_INVALID);
        }
    }
}
