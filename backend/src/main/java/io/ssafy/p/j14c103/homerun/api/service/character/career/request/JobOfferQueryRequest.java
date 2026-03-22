package io.ssafy.p.j14c103.homerun.api.service.character.career.request;

import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareer;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;

public record JobOfferQueryRequest(
    GameCareer gameCareer,
    GameStat gameStat,
    int recentMeetFriendCount
) {

    public JobOfferQueryRequest {
        if (gameCareer == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
        if (gameStat == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
        if (recentMeetFriendCount < 0) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
    }

    public static JobOfferQueryRequest of(
        final GameCareer gameCareer,
        final GameStat gameStat,
        final int recentMeetFriendCount
    ) {
        return new JobOfferQueryRequest(gameCareer, gameStat, recentMeetFriendCount);
    }
}
