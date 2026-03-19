package io.ssafy.p.j14c103.homerun.api.service.world.response;

import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;

@Getter
public class GameTurnResponse {

    private final Integer turnNumber;
    private final LocalDate currentDate;
    private final Integer month;
    private final EconomicCycleResponse economicCycle;
    private final List<NewsResponse> news;

    private GameTurnResponse(
        Integer turnNumber,
        LocalDate currentDate,
        EconomicCycleResponse economicCycle,
        List<NewsResponse> news
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

    public static GameTurnResponse of(
        Integer turnNumber,
        LocalDate currentDate,
        EconomicCycleResponse economicCycle,
        List<NewsResponse> news
    ) {
        return new GameTurnResponse(turnNumber, currentDate, economicCycle, news);
    }

    private void validateTurnNumber(Integer turnNumber) {
        if (turnNumber == null) {
            throw new IllegalArgumentException("turnNumber는 null일 수 없습니다.");
        }
    }

    private void validateCurrentDate(LocalDate currentDate) {
        if (currentDate == null) {
            throw new IllegalArgumentException("currentDate는 null일 수 없습니다.");
        }
    }

    private void validateEconomicCycle(EconomicCycleResponse economicCycle) {
        if (economicCycle == null) {
            throw new IllegalArgumentException("economicCycle은 null일 수 없습니다.");
        }
    }

    private void validateNews(List<NewsResponse> news) {
        if (news == null) {
            throw new IllegalArgumentException("news는 null일 수 없습니다.");
        }
    }

    @Getter
    public static class EconomicCycleResponse {

        private final CyclePhase phase;
        private final String description;

        private EconomicCycleResponse(CyclePhase phase, String description) {
            validatePhase(phase);
            validateDescription(description);

            this.phase = phase;
            this.description = description;
        }

        public static EconomicCycleResponse of(CyclePhase phase, String description) {
            return new EconomicCycleResponse(phase, description);
        }

        private void validatePhase(CyclePhase phase) {
            if (phase == null) {
                throw new IllegalArgumentException("phase는 null일 수 없습니다.");
            }
        }

        private void validateDescription(String description) {
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
