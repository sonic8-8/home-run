package io.ssafy.p.j14c103.homerun.api.service.world.result;

import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import java.util.List;
import lombok.Getter;

@Getter
public class GameWorldResult {

    private final CycleResult cycleResult;
    private final List<NewsCandidate> newsCandidates;
    private final List<EventCandidate> eventCandidates;
    private final HousingSnapshot housingSnapshot;

    private GameWorldResult(
        CycleResult cycleResult,
        List<NewsCandidate> newsCandidates,
        List<EventCandidate> eventCandidates,
        HousingSnapshot housingSnapshot
    ) {
        validateCycleResult(cycleResult);
        validateNewsCandidates(newsCandidates);
        validateEventCandidates(eventCandidates);
        validateHousingSnapshot(housingSnapshot);

        this.cycleResult = cycleResult;
        this.newsCandidates = List.copyOf(newsCandidates);
        this.eventCandidates = List.copyOf(eventCandidates);
        this.housingSnapshot = housingSnapshot;
    }

    public static GameWorldResult of(
        CycleResult cycleResult,
        List<NewsCandidate> newsCandidates,
        List<EventCandidate> eventCandidates,
        HousingSnapshot housingSnapshot
    ) {
        return new GameWorldResult(cycleResult, newsCandidates, eventCandidates, housingSnapshot);
    }

    private void validateCycleResult(CycleResult cycleResult) {
        if (cycleResult == null) {
            throw new IllegalArgumentException("cycleResult는 null일 수 없습니다.");
        }
    }

    private void validateNewsCandidates(List<NewsCandidate> newsCandidates) {
        if (newsCandidates == null) {
            throw new IllegalArgumentException("newsCandidates는 null일 수 없습니다.");
        }
    }

    private void validateEventCandidates(List<EventCandidate> eventCandidates) {
        if (eventCandidates == null) {
            throw new IllegalArgumentException("eventCandidates는 null일 수 없습니다.");
        }
    }

    private void validateHousingSnapshot(HousingSnapshot housingSnapshot) {
        if (housingSnapshot == null) {
            throw new IllegalArgumentException("housingSnapshot은 null일 수 없습니다.");
        }
    }

    @Getter
    public static class CycleResult {

        private final CyclePhase nextPhase;
        private final String description;

        private CycleResult(CyclePhase nextPhase, String description) {
            validateNextPhase(nextPhase);
            validateDescription(description);

            this.nextPhase = nextPhase;
            this.description = description;
        }

        public static CycleResult of(CyclePhase nextPhase, String description) {
            return new CycleResult(nextPhase, description);
        }

        private void validateNextPhase(CyclePhase nextPhase) {
            if (nextPhase == null) {
                throw new IllegalArgumentException("nextPhase는 null일 수 없습니다.");
            }
        }

        private void validateDescription(String description) {
            if (description == null || description.isBlank()) {
                throw new IllegalArgumentException("description은 비어 있을 수 없습니다.");
            }
        }
    }

    @Getter
    public static class NewsCandidate {

        private final Integer newsId;
        private final String headline;

        private NewsCandidate(Integer newsId, String headline) {
            this.newsId = newsId;
            this.headline = headline;
        }

        public static NewsCandidate of(Integer newsId, String headline) {
            return new NewsCandidate(newsId, headline);
        }
    }

    @Getter
    public static class EventCandidate {

        private final Long eventId;
        private final String title;

        private EventCandidate(Long eventId, String title) {
            this.eventId = eventId;
            this.title = title;
        }

        public static EventCandidate of(Long eventId, String title) {
            return new EventCandidate(eventId, title);
        }
    }

    @Getter
    public static class HousingSnapshot {

        private final HousingType currentHousingType;
        private final Long currentPropertyId;
        private final Long targetPropertyId;
        private final boolean hasHousingLossSignal;

        private HousingSnapshot(
            HousingType currentHousingType,
            Long currentPropertyId,
            Long targetPropertyId,
            boolean hasHousingLossSignal
        ) {
            this.currentHousingType = currentHousingType;
            this.currentPropertyId = currentPropertyId;
            this.targetPropertyId = targetPropertyId;
            this.hasHousingLossSignal = hasHousingLossSignal;
        }

        public static HousingSnapshot of(
            HousingType currentHousingType,
            Long currentPropertyId,
            Long targetPropertyId,
            boolean hasHousingLossSignal
        ) {
            return new HousingSnapshot(
                currentHousingType,
                currentPropertyId,
                targetPropertyId,
                hasHousingLossSignal
            );
        }

        public static HousingSnapshot empty() {
            return new HousingSnapshot(null, null, null, false);
        }
    }
}
