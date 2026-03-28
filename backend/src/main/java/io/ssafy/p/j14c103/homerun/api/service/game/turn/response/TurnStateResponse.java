package io.ssafy.p.j14c103.homerun.api.service.game.turn.response;

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

        public static List<NewsResponse> emptyList() {
            return List.of();
        }
    }
}
