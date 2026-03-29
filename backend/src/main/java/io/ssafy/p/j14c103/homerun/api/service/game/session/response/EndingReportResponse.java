package io.ssafy.p.j14c103.homerun.api.service.game.session.response;

import io.ssafy.p.j14c103.homerun.api.service.world.ending.response.WorldEndingHistoryProviderResponse;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.SessionStatus;
import io.ssafy.p.j14c103.homerun.domain.gamesession.report.GameReport;
import io.ssafy.p.j14c103.homerun.domain.gamesession.report.GameReportAchievement;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import lombok.Getter;

@Getter
public class EndingReportResponse {

    private final CharacterType characterType;
    private final SessionStatus endingType;
    private final String grade;
    private final String title;
    private final Long totalAssets;
    private final Long totalIncome;
    private final Long totalExpense;
    private final Long netProfit;
    private final SpendingPatternResponse spendingPattern;
    private final List<AchievementResponse> achievements;
    private final List<NewsHistoryResponse> newsHistories;
    private final List<EventHistoryResponse> eventHistories;
    private final List<HousingHistoryResponse> housingHistories;
    private final HousingSnapshotResponse housingSnapshot;

    private EndingReportResponse(
        final CharacterType characterType,
        final SessionStatus endingType,
        final String grade,
        final String title,
        final Long totalAssets,
        final Long totalIncome,
        final Long totalExpense,
        final Long netProfit,
        final SpendingPatternResponse spendingPattern,
        final List<AchievementResponse> achievements,
        final List<NewsHistoryResponse> newsHistories,
        final List<EventHistoryResponse> eventHistories,
        final List<HousingHistoryResponse> housingHistories,
        final HousingSnapshotResponse housingSnapshot
    ) {
        this.characterType = characterType;
        this.endingType = endingType;
        this.grade = grade;
        this.title = title;
        this.totalAssets = totalAssets;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.netProfit = netProfit;
        this.spendingPattern = spendingPattern;
        this.achievements = List.copyOf(achievements);
        this.newsHistories = List.copyOf(newsHistories);
        this.eventHistories = List.copyOf(eventHistories);
        this.housingHistories = List.copyOf(housingHistories);
        this.housingSnapshot = housingSnapshot;
    }

    public static EndingReportResponse of(
        final CharacterType characterType,
        final GameReport gameReport,
        final WorldEndingHistoryProviderResponse history
    ) {
        return create(
            characterType,
            gameReport.getEndingType(),
            gameReport.getGrade(),
            gameReport.getEndingTitle(),
            toLong(gameReport.getTotalAssetsAmount()),
            toLong(gameReport.getTotalIncomeAmount()),
            toLong(gameReport.getTotalExpenseAmount()),
            toLong(gameReport.getNetProfitAmount()),
            SpendingPatternResponse.of(
                gameReport.getTopSpendingCategory(),
                gameReport.getTopSpendingRatio()
            ),
            safeList(gameReport.getAchievements()).stream()
                .map(AchievementResponse::from)
                .toList(),
            history.getNewsHistories().stream()
                .map(NewsHistoryResponse::from)
                .toList(),
            history.getEventHistories().stream()
                .map(EventHistoryResponse::from)
                .toList(),
            history.getHousingHistories().stream()
                .map(HousingHistoryResponse::from)
                .toList(),
            HousingSnapshotResponse.from(history.getHousingSnapshot())
        );
    }

    public static EndingReportResponse create(
        final CharacterType characterType,
        final SessionStatus endingType,
        final String grade,
        final String title,
        final Long totalAssets,
        final Long totalIncome,
        final Long totalExpense,
        final Long netProfit,
        final SpendingPatternResponse spendingPattern,
        final List<AchievementResponse> achievements,
        final List<NewsHistoryResponse> newsHistories,
        final List<EventHistoryResponse> eventHistories,
        final List<HousingHistoryResponse> housingHistories,
        final HousingSnapshotResponse housingSnapshot
    ) {
        return new EndingReportResponse(
            characterType,
            endingType,
            grade,
            title,
            totalAssets,
            totalIncome,
            totalExpense,
            netProfit,
            spendingPattern,
            achievements,
            newsHistories,
            eventHistories,
            housingHistories,
            housingSnapshot
        );
    }

    private static Long toLong(final Integer amount) {
        return amount == null ? null : amount.longValue();
    }

    private static <T> List<T> safeList(final List<T> values) {
        return values == null ? Collections.emptyList() : values;
    }

    @Getter
    public static class SpendingPatternResponse {

        private final String topCategory;
        private final BigDecimal topCategoryRatio;

        private SpendingPatternResponse(
            final String topCategory,
            final BigDecimal topCategoryRatio
        ) {
            this.topCategory = topCategory;
            this.topCategoryRatio = topCategoryRatio;
        }

        public static SpendingPatternResponse of(
            final String topCategory,
            final BigDecimal topCategoryRatio
        ) {
            return new SpendingPatternResponse(topCategory, topCategoryRatio);
        }
    }

    @Getter
    public static class AchievementResponse {

        private final String name;
        private final String iconUrl;

        private AchievementResponse(
            final String name,
            final String iconUrl
        ) {
            this.name = name;
            this.iconUrl = iconUrl;
        }

        public static AchievementResponse from(final GameReportAchievement achievement) {
            return new AchievementResponse(achievement.getName(), achievement.getIconUrl());
        }

        public static AchievementResponse of(
            final String name,
            final String iconUrl
        ) {
            return new AchievementResponse(name, iconUrl);
        }
    }

    @Getter
    public static class NewsHistoryResponse {

        private final Integer turnNumber;
        private final String newsId;
        private final String headline;
        private final LocalDate publishedDate;

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

        public static NewsHistoryResponse from(
            final WorldEndingHistoryProviderResponse.NewsHistoryItem item
        ) {
            return new NewsHistoryResponse(
                item.getTurnNumber(),
                item.getNewsId(),
                item.getHeadline(),
                item.getPublishedDate()
            );
        }

        public static NewsHistoryResponse of(
            final Integer turnNumber,
            final String newsId,
            final String headline,
            final LocalDate publishedDate
        ) {
            return new NewsHistoryResponse(turnNumber, newsId, headline, publishedDate);
        }
    }

    @Getter
    public static class EventHistoryResponse {

        private final Integer turnNumber;
        private final Integer gameEventId;
        private final String selectedChoiceCode;
        private final String resultSummary;
        private final LocalDateTime resolvedAt;

        private EventHistoryResponse(
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

        public static EventHistoryResponse from(
            final WorldEndingHistoryProviderResponse.EventHistoryItem item
        ) {
            return new EventHistoryResponse(
                item.getTurnNumber(),
                item.getGameEventId(),
                item.getSelectedChoiceCode(),
                item.getResultSummary(),
                item.getResolvedAt()
            );
        }

        public static EventHistoryResponse of(
            final Integer turnNumber,
            final Integer gameEventId,
            final String selectedChoiceCode,
            final String resultSummary,
            final LocalDateTime resolvedAt
        ) {
            return new EventHistoryResponse(
                turnNumber,
                gameEventId,
                selectedChoiceCode,
                resultSummary,
                resolvedAt
            );
        }
    }

    @Getter
    public static class HousingSnapshotResponse {

        private final HousingType currentHousingType;
        private final Long currentPropertyId;
        private final Long targetPropertyId;

        private HousingSnapshotResponse(
            final HousingType currentHousingType,
            final Long currentPropertyId,
            final Long targetPropertyId
        ) {
            this.currentHousingType = currentHousingType;
            this.currentPropertyId = currentPropertyId;
            this.targetPropertyId = targetPropertyId;
        }

        public static HousingSnapshotResponse from(
            final WorldEndingHistoryProviderResponse.HousingSnapshotItem item
        ) {
            return new HousingSnapshotResponse(
                item.getCurrentHousingType(),
                item.getCurrentPropertyId(),
                item.getTargetPropertyId()
            );
        }

        public static HousingSnapshotResponse of(
            final HousingType currentHousingType,
            final Long currentPropertyId,
            final Long targetPropertyId
        ) {
            return new HousingSnapshotResponse(currentHousingType, currentPropertyId, targetPropertyId);
        }
    }

    @Getter
    public static class HousingHistoryResponse {

        private final Integer turnNumber;
        private final String summary;
        private final HousingStateResponse beforeState;
        private final HousingStateResponse afterState;

        private HousingHistoryResponse(
            final Integer turnNumber,
            final String summary,
            final HousingStateResponse beforeState,
            final HousingStateResponse afterState
        ) {
            this.turnNumber = turnNumber;
            this.summary = summary;
            this.beforeState = beforeState;
            this.afterState = afterState;
        }

        public static HousingHistoryResponse from(
            final WorldEndingHistoryProviderResponse.HousingHistoryItem item
        ) {
            return new HousingHistoryResponse(
                item.getTurnNumber(),
                item.getSummary(),
                HousingStateResponse.from(item.getBeforeState()),
                HousingStateResponse.from(item.getAfterState())
            );
        }

        public static HousingHistoryResponse of(
            final Integer turnNumber,
            final String summary,
            final HousingStateResponse beforeState,
            final HousingStateResponse afterState
        ) {
            return new HousingHistoryResponse(turnNumber, summary, beforeState, afterState);
        }
    }

    @Getter
    public static class HousingStateResponse {

        private final HousingType housingType;
        private final Long propertyId;

        private HousingStateResponse(
            final HousingType housingType,
            final Long propertyId
        ) {
            this.housingType = housingType;
            this.propertyId = propertyId;
        }

        public static HousingStateResponse from(
            final WorldEndingHistoryProviderResponse.HousingStateItem item
        ) {
            return new HousingStateResponse(item.getHousingType(), item.getPropertyId());
        }

        public static HousingStateResponse of(
            final HousingType housingType,
            final Long propertyId
        ) {
            return new HousingStateResponse(housingType, propertyId);
        }
    }
}
