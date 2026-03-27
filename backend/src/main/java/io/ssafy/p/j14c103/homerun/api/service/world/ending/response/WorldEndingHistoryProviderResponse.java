package io.ssafy.p.j14c103.homerun.api.service.world.ending.response;

import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;

@Getter
public class WorldEndingHistoryProviderResponse {

    private final List<NewsHistoryItem> newsHistories;
    private final List<EventHistoryItem> eventHistories;
    private final HousingSnapshotItem housingSnapshot;

    private WorldEndingHistoryProviderResponse(
        final List<NewsHistoryItem> newsHistories,
        final List<EventHistoryItem> eventHistories,
        final HousingSnapshotItem housingSnapshot
    ) {
        this.newsHistories = List.copyOf(newsHistories);
        this.eventHistories = List.copyOf(eventHistories);
        this.housingSnapshot = housingSnapshot;
    }

    public static WorldEndingHistoryProviderResponse of(
        final List<NewsHistoryItem> newsHistories,
        final List<EventHistoryItem> eventHistories,
        final HousingSnapshotItem housingSnapshot
    ) {
        return new WorldEndingHistoryProviderResponse(newsHistories, eventHistories, housingSnapshot);
    }

    @Getter
    public static class NewsHistoryItem {

        private final Integer turnNumber;
        private final String newsId;
        private final String headline;
        private final LocalDate publishedDate;

        private NewsHistoryItem(
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

        public static NewsHistoryItem of(
            final Integer turnNumber,
            final String newsId,
            final String headline,
            final LocalDate publishedDate
        ) {
            return new NewsHistoryItem(turnNumber, newsId, headline, publishedDate);
        }
    }

    @Getter
    public static class EventHistoryItem {

        private final Integer turnNumber;
        private final Integer gameEventId;
        private final String selectedChoiceCode;
        private final String resultSummary;
        private final LocalDateTime resolvedAt;

        private EventHistoryItem(
            final Integer turnNumber,
            final Integer gameEventId,
            final String selectedChoiceCode,
            final String resultSummary,
            final LocalDateTime resolvedAt
        ) {
            this.turnNumber = turnNumber;
            this.gameEventId = gameEventId;
            this.selectedChoiceCode = selectedChoiceCode;
            this.resultSummary = resultSummary;
            this.resolvedAt = resolvedAt;
        }

        public static EventHistoryItem of(
            final Integer turnNumber,
            final Integer gameEventId,
            final String selectedChoiceCode,
            final String resultSummary,
            final LocalDateTime resolvedAt
        ) {
            return new EventHistoryItem(
                turnNumber,
                gameEventId,
                selectedChoiceCode,
                resultSummary,
                resolvedAt
            );
        }
    }

    @Getter
    public static class HousingSnapshotItem {

        private final HousingType currentHousingType;
        private final Long currentPropertyId;
        private final Long targetPropertyId;

        private HousingSnapshotItem(
            final HousingType currentHousingType,
            final Long currentPropertyId,
            final Long targetPropertyId
        ) {
            this.currentHousingType = currentHousingType;
            this.currentPropertyId = currentPropertyId;
            this.targetPropertyId = targetPropertyId;
        }

        public static HousingSnapshotItem of(
            final HousingType currentHousingType,
            final Long currentPropertyId,
            final Long targetPropertyId
        ) {
            return new HousingSnapshotItem(currentHousingType, currentPropertyId, targetPropertyId);
        }
    }
}
