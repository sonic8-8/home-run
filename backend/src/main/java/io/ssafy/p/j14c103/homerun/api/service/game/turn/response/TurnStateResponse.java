package io.ssafy.p.j14c103.homerun.api.service.game.turn.response;

import io.ssafy.p.j14c103.homerun.api.service.world.response.LatestTurnNewsResponse;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class TurnStateResponse {

    private final Integer turnNumber;
    private final LocalDate currentDate;
    private final Integer month;
    private final EconomicCycleResponse economicCycle;
    private final List<NewsResponse> news;

    @Builder(access = AccessLevel.PRIVATE)
    private TurnStateResponse(
        final Integer turnNumber,
        final LocalDate currentDate,
        final EconomicCycleResponse economicCycle,
        final List<NewsResponse> news
    ) {
        this.turnNumber = Objects.requireNonNull(turnNumber, "turnNumber는 null일 수 없습니다.");
        this.currentDate = Objects.requireNonNull(currentDate, "currentDate는 null일 수 없습니다.");
        this.month = this.currentDate.getMonthValue();
        this.economicCycle = Objects.requireNonNull(economicCycle, "economicCycle은 null일 수 없습니다.");
        this.news = List.copyOf(Objects.requireNonNull(news, "news는 null일 수 없습니다."));
    }

    public static TurnStateResponse of(
        final Integer turnNumber,
        final LocalDate currentDate,
        final EconomicCycleResponse economicCycle,
        final List<NewsResponse> news
    ) {
        return TurnStateResponse.builder()
            .turnNumber(turnNumber)
            .currentDate(currentDate)
            .economicCycle(economicCycle)
            .news(news)
            .build();
    }

    @Getter
    public static class EconomicCycleResponse {

        private final CyclePhase phase;
        private final String description;

        @Builder(access = AccessLevel.PRIVATE)
        private EconomicCycleResponse(final CyclePhase phase, final String description) {
            this.phase = Objects.requireNonNull(phase, "phase는 null일 수 없습니다.");
            if (description == null || description.isBlank()) {
                throw new IllegalArgumentException("description은 비어 있을 수 없습니다.");
            }
            this.description = description;
        }

        public static EconomicCycleResponse of(final CyclePhase phase, final String description) {
            return EconomicCycleResponse.builder()
                .phase(phase)
                .description(description)
                .build();
        }
    }

    @Getter
    public static class NewsResponse {

        private final String newsId;
        private final String headline;
        private final String content;
        private final String sourceName;
        private final LocalDate publishedDate;
        private final String economicCycleType;

        @Builder(access = AccessLevel.PRIVATE)
        private NewsResponse(
            final String newsId,
            final String headline,
            final String content,
            final String sourceName,
            final LocalDate publishedDate,
            final String economicCycleType
        ) {
            this.newsId = newsId;
            this.headline = headline;
            this.content = content;
            this.sourceName = sourceName;
            this.publishedDate = publishedDate;
            this.economicCycleType = economicCycleType;
        }

        public static NewsResponse of(
            final String newsId,
            final String headline,
            final String content,
            final String sourceName,
            final LocalDate publishedDate,
            final String economicCycleType
        ) {
            return NewsResponse.builder()
                .newsId(newsId)
                .headline(headline)
                .content(content)
                .sourceName(sourceName)
                .publishedDate(publishedDate)
                .economicCycleType(economicCycleType)
                .build();
        }

        public static NewsResponse from(final LatestTurnNewsResponse.NewsItemResponse item) {
            return NewsResponse.of(
                item.getNewsId(),
                item.getHeadline(),
                item.getContent(),
                item.getSourceName(),
                item.getPublishedDate(),
                item.getEconomicCycleType()
            );
        }

        public static List<NewsResponse> from(
            final List<LatestTurnNewsResponse.NewsItemResponse> items
        ) {
            return items.stream()
                .map(NewsResponse::from)
                .toList();
        }

        public static List<NewsResponse> emptyList() {
            return List.of();
        }
    }
}
