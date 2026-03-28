package io.ssafy.p.j14c103.homerun.api.service.game.turn.response;

import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;

@Getter
public class TurnStateResponse {

    private final Integer turnNumber;
    private final LocalDate currentDate;
    private final Integer month;
    private final EconomicCycleResponse economicCycle;
    private final List<NewsResponse> news;

    private TurnStateResponse(
        final Integer turnNumber,
        final LocalDate currentDate,
        final EconomicCycleResponse economicCycle,
        final List<NewsResponse> news
    ) {
        validateTurnNumber(turnNumber);
        validateCurrentDate(currentDate);
        validateEconomicCycle(economicCycle);
        validateNews(news);
        this.turnNumber = turnNumber;
        this.currentDate = currentDate;
        this.month = currentDate.getMonthValue();
        this.economicCycle = economicCycle;
        this.news = List.copyOf(news);
    }

    public static TurnStateResponse of(
        final Integer turnNumber,
        final LocalDate currentDate,
        final EconomicCycleResponse economicCycle,
        final List<NewsResponse> news
    ) {
        return new TurnStateResponse(turnNumber, currentDate, economicCycle, news);
    }

    private void validateTurnNumber(final Integer turnNumber) {
        if (turnNumber == null) {
            throw new IllegalArgumentException("turnNumber는 null일 수 없습니다.");
        }
    }

    private void validateCurrentDate(final LocalDate currentDate) {
        if (currentDate == null) {
            throw new IllegalArgumentException("currentDate는 null일 수 없습니다.");
        }
    }

    private void validateEconomicCycle(final EconomicCycleResponse economicCycle) {
        if (economicCycle == null) {
            throw new IllegalArgumentException("economicCycle은 null일 수 없습니다.");
        }
    }

    private void validateNews(final List<NewsResponse> news) {
        if (news == null) {
            throw new IllegalArgumentException("news는 null일 수 없습니다.");
        }
    }

    @Getter
    public static class EconomicCycleResponse {

        private final CyclePhase phase;
        private final String description;

        private EconomicCycleResponse(final CyclePhase phase, final String description) {
            validatePhase(phase);
            validateDescription(description);
            this.phase = phase;
            this.description = description;
        }

        public static EconomicCycleResponse of(final CyclePhase phase, final String description) {
            return new EconomicCycleResponse(phase, description);
        }

        private void validatePhase(final CyclePhase phase) {
            if (phase == null) {
                throw new IllegalArgumentException("phase는 null일 수 없습니다.");
            }
        }

        private void validateDescription(final String description) {
            if (description == null || description.isBlank()) {
                throw new IllegalArgumentException("description은 비어 있을 수 없습니다.");
            }
        }
    }

    @Getter
    public static class NewsResponse {

        public static List<NewsResponse> emptyList() {
            return List.of();
        }
    }
}
