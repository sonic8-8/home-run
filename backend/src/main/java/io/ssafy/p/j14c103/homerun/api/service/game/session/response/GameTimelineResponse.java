package io.ssafy.p.j14c103.homerun.api.service.game.session.response;

import io.ssafy.p.j14c103.homerun.domain.gamesession.report.GameTimeline;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;

@Getter
public class GameTimelineResponse {

    private final List<TimelineItemResponse> timeline;

    private GameTimelineResponse(final List<TimelineItemResponse> timeline) {
        this.timeline = List.copyOf(timeline);
    }

    public static GameTimelineResponse from(final List<GameTimeline> gameTimelines) {
        return new GameTimelineResponse(
            gameTimelines.stream()
                .map(TimelineItemResponse::from)
                .toList()
        );
    }

    public static GameTimelineResponse of(final List<TimelineItemResponse> timeline) {
        return new GameTimelineResponse(timeline);
    }

    @Getter
    public static class TimelineItemResponse {

        private final Integer turnNumber;
        private final LocalDate date;
        private final Long cash;
        private final Long netAssets;
        private final Long totalAssets;
        private final Long stockValue;
        private final Long loanBalance;
        private final Long salary;

        private TimelineItemResponse(
            final Integer turnNumber,
            final LocalDate date,
            final Long cash,
            final Long netAssets,
            final Long totalAssets,
            final Long stockValue,
            final Long loanBalance,
            final Long salary
        ) {
            this.turnNumber = turnNumber;
            this.date = date;
            this.cash = cash;
            this.netAssets = netAssets;
            this.totalAssets = totalAssets;
            this.stockValue = stockValue;
            this.loanBalance = loanBalance;
            this.salary = salary;
        }

        public static TimelineItemResponse from(final GameTimeline gameTimeline) {
            return new TimelineItemResponse(
                gameTimeline.getTurnNumber(),
                gameTimeline.getLoggedDate(),
                toLong(gameTimeline.getCash()),
                toLong(gameTimeline.getNetAssets()),
                toLong(gameTimeline.getTotalAssets()),
                toLong(gameTimeline.getStockValueAmount()),
                toLong(gameTimeline.getLoanBalanceAmount()),
                toLong(gameTimeline.getSalaryAmount())
            );
        }

        public static TimelineItemResponse of(
            final Integer turnNumber,
            final LocalDate date,
            final Long cash,
            final Long netAssets,
            final Long totalAssets,
            final Long stockValue,
            final Long loanBalance,
            final Long salary
        ) {
            return new TimelineItemResponse(
                turnNumber,
                date,
                cash,
                netAssets,
                totalAssets,
                stockValue,
                loanBalance,
                salary
            );
        }

        private static Long toLong(final Integer amount) {
            return amount == null ? null : amount.longValue();
        }
    }
}
