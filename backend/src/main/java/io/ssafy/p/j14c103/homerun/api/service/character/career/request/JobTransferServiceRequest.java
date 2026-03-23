package io.ssafy.p.j14c103.homerun.api.service.character.career.request;

import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareer;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;

public record JobTransferServiceRequest(
    GameCareer gameCareer,
    GameStat gameStat,
    int recentMeetFriendCount,
    String offerId,
    int currentTurn
) {

    public JobTransferServiceRequest {
        if (gameCareer == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
        if (gameStat == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
        if (recentMeetFriendCount < 0) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
        if (offerId == null || offerId.isBlank()) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
        if (currentTurn < 1) {
            throw new HomerunException(ErrorCode.CHARACTER_TURN_INVALID);
        }
    }

    public static JobTransferServiceRequest of(
        final GameCareer gameCareer,
        final GameStat gameStat,
        final int recentMeetFriendCount,
        final String offerId,
        final int currentTurn
    ) {
        return new JobTransferServiceRequest(
            gameCareer,
            gameStat,
            recentMeetFriendCount,
            offerId,
            currentTurn
        );
    }
}
