package io.ssafy.p.j14c103.homerun.api.service.world.response;

import io.ssafy.p.j14c103.homerun.api.service.world.result.GameSessionNewsResult;
import io.ssafy.p.j14c103.homerun.domain.world.news.NewsMaster;
import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class LatestTurnNewsResponse {

    private final Integer turnNumber;
    private final LocalDate currentDate;
    private final List<NewsItemResponse> news;

    @Builder(access = AccessLevel.PRIVATE)
    private LatestTurnNewsResponse(
        final Integer turnNumber,
        final LocalDate currentDate,
        final List<NewsItemResponse> news
    ) {
        this.turnNumber = turnNumber;
        this.currentDate = currentDate;
        this.news = List.copyOf(news);
    }

    public static LatestTurnNewsResponse of(
        final Integer turnNumber,
        final LocalDate currentDate,
        final List<NewsItemResponse> news
    ) {
        return LatestTurnNewsResponse.builder()
            .turnNumber(turnNumber)
            .currentDate(currentDate)
            .news(news)
            .build();
    }

    public static LatestTurnNewsResponse from(final GameSessionNewsResult result) {
        return LatestTurnNewsResponse.of(
            result.getTurnNumber(),
            result.getCurrentDate(),
            result.getNews().stream()
                .map(NewsItemResponse::from)
                .toList()
        );
    }

    @Getter
    public static class NewsItemResponse {

        private final String newsId;
        private final String headline;
        private final String content;
        private final String sourceName;
        private final LocalDate publishedDate;
        private final String economicCycleType;

        @Builder(access = AccessLevel.PRIVATE)
        private NewsItemResponse(
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

        public static NewsItemResponse of(
            final String newsId,
            final String headline,
            final String content,
            final String sourceName,
            final LocalDate publishedDate,
            final String economicCycleType
        ) {
            return NewsItemResponse.builder()
                .newsId(newsId)
                .headline(headline)
                .content(content)
                .sourceName(sourceName)
                .publishedDate(publishedDate)
                .economicCycleType(economicCycleType)
                .build();
        }

        public static NewsItemResponse from(
            final NewsMaster newsMaster,
            final LocalDate publishedDate
        ) {
            return NewsItemResponse.of(
                newsMaster.getNewsId(),
                newsMaster.getTitle(),
                newsMaster.getArticleText(),
                newsMaster.getSourceName(),
                publishedDate,
                newsMaster.getEconomicCycleType()
            );
        }

        public static NewsItemResponse from(final GameSessionNewsResult.NewsItem newsItem) {
            return NewsItemResponse.of(
                newsItem.getNewsId(),
                newsItem.getHeadline(),
                newsItem.getContent(),
                newsItem.getSourceName(),
                newsItem.getPublishedDate(),
                newsItem.getEconomicCycleType()
            );
        }
    }
}
