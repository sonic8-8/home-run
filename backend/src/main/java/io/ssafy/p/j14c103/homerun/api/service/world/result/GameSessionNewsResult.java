package io.ssafy.p.j14c103.homerun.api.service.world.result;

import java.time.LocalDate;
import java.util.List;
import lombok.Getter;

@Getter
public class GameSessionNewsResult {

    private final Integer turnNumber;
    private final LocalDate currentDate;
    private final List<NewsItem> news;

    private GameSessionNewsResult(
        final Integer turnNumber,
        final LocalDate currentDate,
        final List<NewsItem> news
    ) {
        this.turnNumber = turnNumber;
        this.currentDate = currentDate;
        this.news = List.copyOf(news);
    }

    public static GameSessionNewsResult of(
        final Integer turnNumber,
        final LocalDate currentDate,
        final List<NewsItem> news
    ) {
        return new GameSessionNewsResult(turnNumber, currentDate, news);
    }

    @Getter
    public static class NewsItem {

        private final String newsId;
        private final String headline;
        private final String content;
        private final String sourceName;
        private final LocalDate publishedDate;
        private final String economicCycleType;

        private NewsItem(
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

        public static NewsItem of(
            final String newsId,
            final String headline,
            final String content,
            final String sourceName,
            final LocalDate publishedDate,
            final String economicCycleType
        ) {
            return new NewsItem(
                newsId,
                headline,
                content,
                sourceName,
                publishedDate,
                economicCycleType
            );
        }
    }
}
