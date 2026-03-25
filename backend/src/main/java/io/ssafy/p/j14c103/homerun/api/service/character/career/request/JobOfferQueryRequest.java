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

    @Builder(access = AccessLevel.PRIVATE)
    private JobOfferQueryRequest(
        final GameCareer gameCareer,
        final GameStat gameStat,
        final int recentMeetFriendCount
    ) {
        validateRequest(gameCareer, gameStat, recentMeetFriendCount);

        this.gameCareer = gameCareer;
        this.gameStat = gameStat;
        this.recentMeetFriendCount = recentMeetFriendCount;
    }

    public static JobOfferQueryRequest of(
        final GameCareer gameCareer,
        final GameStat gameStat,
        final int recentMeetFriendCount
    ) {
        return JobOfferQueryRequest.builder()
            .gameCareer(gameCareer)
            .gameStat(gameStat)
            .recentMeetFriendCount(recentMeetFriendCount)
            .build();
    }

    public GameCareer gameCareer() {
        return gameCareer;
    }

    public GameStat gameStat() {
        return gameStat;
    }

    public int recentMeetFriendCount() {
        return recentMeetFriendCount;
    }

    private void validateRequest(
        final GameCareer gameCareer,
        final GameStat gameStat,
        final int recentMeetFriendCount
    ) {
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
}
