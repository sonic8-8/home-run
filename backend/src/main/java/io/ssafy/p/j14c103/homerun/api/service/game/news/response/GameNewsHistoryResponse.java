package io.ssafy.p.j14c103.homerun.api.service.game.news.response;

import io.ssafy.p.j14c103.homerun.api.service.world.ending.response.WorldEndingHistoryProviderResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class GameNewsHistoryResponse {

    private final List<NewsHistoryResponse> newsHistories;

    @Builder(access = AccessLevel.PRIVATE)
    private GameNewsHistoryResponse(final List<NewsHistoryResponse> newsHistories) {
        this.newsHistories = List.copyOf(Objects.requireNonNull(newsHistories));
    }

    public static GameNewsHistoryResponse of(final List<NewsHistoryResponse> newsHistories) {
        return GameNewsHistoryResponse.builder()
            .newsHistories(newsHistories)
            .build();
    }

    @Getter
    public static class NewsHistoryResponse {

        private final Integer turnNumber;
        private final String newsId;
        private final String headline;
        private final LocalDate publishedDate;

        @Builder(access = AccessLevel.PRIVATE)
        private NewsHistoryResponse(
            final Integer turnNumber,
            final String newsId,
            final String headline,
            final LocalDate publishedDate
        ) {
            this.turnNumber = turnNumber;
            this.newsId = newsId;
            this.headline = headline;
            this.publishedDate = publishedDate;
        }

        public static NewsHistoryResponse of(
            final Integer turnNumber,
            final String newsId,
            final String headline,
            final LocalDate publishedDate
        ) {
            return NewsHistoryResponse.builder()
                .turnNumber(turnNumber)
                .newsId(newsId)
                .headline(headline)
                .publishedDate(publishedDate)
                .build();
        }

        public static NewsHistoryResponse from(
            final WorldEndingHistoryProviderResponse.NewsHistoryItem item
        ) {
            return NewsHistoryResponse.of(
                item.getTurnNumber(),
                item.getNewsId(),
                item.getHeadline(),
                item.getPublishedDate()
            );
        }
    }
}
