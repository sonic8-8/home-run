package io.ssafy.p.j14c103.homerun.domain.gamesession.report;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameReportAchievement {

    private String name;
    private String iconUrl;

    private GameReportAchievement(
        final String name,
        final String iconUrl
    ) {
        this.name = name;
        this.iconUrl = iconUrl;
    }

    public static GameReportAchievement of(
        final String name,
        final String iconUrl
    ) {
        return new GameReportAchievement(name, iconUrl);
    }
}
