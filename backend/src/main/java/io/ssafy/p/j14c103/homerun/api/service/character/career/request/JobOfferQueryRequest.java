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
public class JobOfferQueryRequest {

    private GameCareer gameCareer;
    private GameStat gameStat;
    private int recentMeetFriendCount;
    private int currentTurn;

    @Builder(access = AccessLevel.PRIVATE)
    private JobOfferQueryRequest(
        final GameCareer gameCareer,
        final GameStat gameStat,
        final int recentMeetFriendCount,
        final int currentTurn
    ) {
        validateRequest(gameCareer, gameStat, recentMeetFriendCount, currentTurn);

        this.gameCareer = gameCareer;
        this.gameStat = gameStat;
        this.recentMeetFriendCount = recentMeetFriendCount;
        this.currentTurn = currentTurn;
    }

    public static JobOfferQueryRequest of(
        final GameCareer gameCareer,
        final GameStat gameStat,
        final int recentMeetFriendCount,
        final int currentTurn
    ) {
        return JobOfferQueryRequest.builder()
            .gameCareer(gameCareer)
            .gameStat(gameStat)
            .recentMeetFriendCount(recentMeetFriendCount)
            .currentTurn(currentTurn)
            .build();
    }

    private void validateRequest(
        final GameCareer gameCareer,
        final GameStat gameStat,
        final int recentMeetFriendCount,
        final int currentTurn
    ) {
        if (gameCareer == null || gameStat == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
        if (recentMeetFriendCount < 0) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
        if (currentTurn < 1) {
            throw new HomerunException(ErrorCode.CHARACTER_TURN_INVALID);
        }
    }
}
