package io.ssafy.p.j14c103.homerun.api.service.world.result;

import io.ssafy.p.j14c103.homerun.domain.world.event.EventPresentationType;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
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
            throw new HomerunException(ErrorCode.WORLD_RESULT_INVALID);
        }
    }

    private void validateNewsCandidates(List<NewsCandidate> newsCandidates) {
        if (newsCandidates == null) {
            throw new HomerunException(ErrorCode.WORLD_RESULT_INVALID);
        }
    }

    private void validateEventCandidates(List<EventCandidate> eventCandidates) {
        if (eventCandidates == null) {
            throw new HomerunException(ErrorCode.WORLD_RESULT_INVALID);
        }
    }

    private void validateHousingSnapshot(HousingSnapshot housingSnapshot) {
        if (housingSnapshot == null) {
            throw new HomerunException(ErrorCode.WORLD_RESULT_INVALID);
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
                throw new HomerunException(ErrorCode.WORLD_RESULT_INVALID);
            }
        }

        private void validateDescription(String description) {
            if (description == null || description.isBlank()) {
                throw new HomerunException(ErrorCode.WORLD_RESULT_INVALID);
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

        private final Integer gameEventId;
        private final String eventCode;
        private final String eventName;
        private final EventPresentationType eventPresentationType;

        private EventCandidate(
            final Integer gameEventId,
            final String eventCode,
            final String eventName,
            final EventPresentationType eventPresentationType
        ) {
            validateGameEventId(gameEventId);
            validateEventCode(eventCode);
            validateEventName(eventName);
            validateEventPresentationType(eventPresentationType);

            this.gameEventId = gameEventId;
            this.eventCode = eventCode;
            this.eventName = eventName;
            this.eventPresentationType = eventPresentationType;
        }

        public static EventCandidate of(
            final Integer gameEventId,
            final String eventCode,
            final String eventName,
            final EventPresentationType eventPresentationType
        ) {
            return new EventCandidate(gameEventId, eventCode, eventName, eventPresentationType);
        }

        private void validateGameEventId(final Integer gameEventId) {
            if (gameEventId == null) {
                throw new HomerunException(ErrorCode.WORLD_RESULT_INVALID);
            }
        }

        private void validateEventCode(final String eventCode) {
            if (eventCode == null || eventCode.isBlank()) {
                throw new HomerunException(ErrorCode.WORLD_RESULT_INVALID);
            }
        }

        private void validateEventName(final String eventName) {
            if (eventName == null || eventName.isBlank()) {
                throw new HomerunException(ErrorCode.WORLD_RESULT_INVALID);
            }
        }

        private void validateEventPresentationType(
            final EventPresentationType eventPresentationType
        ) {
            if (eventPresentationType == null) {
                throw new HomerunException(ErrorCode.WORLD_RESULT_INVALID);
            }
        }
    }

    @Getter
    public static class HousingSnapshot {

        private final HousingType currentHousingType;
        private final Integer currentPropertyId;
        private final Integer targetPropertyId;
        private final boolean hasHousingLossSignal;

        private HousingSnapshot(
            HousingType currentHousingType,
            Integer currentPropertyId,
            Integer targetPropertyId,
            boolean hasHousingLossSignal
        ) {
            this.currentHousingType = currentHousingType;
            this.currentPropertyId = currentPropertyId;
            this.targetPropertyId = targetPropertyId;
            this.hasHousingLossSignal = hasHousingLossSignal;
        }

        public static HousingSnapshot of(
            HousingType currentHousingType,
            Integer currentPropertyId,
            Integer targetPropertyId,
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
