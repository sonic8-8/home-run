package io.ssafy.p.j14c103.homerun.api.service.game.turn.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CommitTurnResponse {

    private final Integer turnNumber;
    private final List<SettlementLogItemResponse> settlementLog;
    private final UpdatedAssetsResponse updatedAssets;
    private final StatChangesResponse statChanges;
    private final FlagsResponse flags;

    @Builder(access = AccessLevel.PRIVATE)
    private CommitTurnResponse(
        final Integer turnNumber,
        final List<SettlementLogItemResponse> settlementLog,
        final UpdatedAssetsResponse updatedAssets,
        final StatChangesResponse statChanges,
        final FlagsResponse flags
    ) {
        if (turnNumber == null || settlementLog == null || updatedAssets == null
            || statChanges == null || flags == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
        this.turnNumber = turnNumber;
        this.settlementLog = List.copyOf(settlementLog);
        this.updatedAssets = updatedAssets;
        this.statChanges = statChanges;
        this.flags = flags;
    }

    public static CommitTurnResponse of(
        final Integer turnNumber,
        final List<SettlementLogItemResponse> settlementLog,
        final UpdatedAssetsResponse updatedAssets,
        final StatChangesResponse statChanges,
        final FlagsResponse flags
    ) {
        return CommitTurnResponse.builder()
            .turnNumber(turnNumber)
            .settlementLog(settlementLog)
            .updatedAssets(updatedAssets)
            .statChanges(statChanges)
            .flags(flags)
            .build();
    }

    @Getter
    public static class SettlementLogItemResponse {

        private final String phase;
        private final String description;
        private final Long cashChange;
        private final StatChangesResponse statChanges;

        @Builder(access = AccessLevel.PRIVATE)
        private SettlementLogItemResponse(
            final String phase,
            final String description,
            final Long cashChange,
            final StatChangesResponse statChanges
        ) {
            if (phase == null || phase.isBlank() || description == null || description.isBlank()
                || cashChange == null || statChanges == null) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
            this.phase = phase;
            this.description = description;
            this.cashChange = cashChange;
            this.statChanges = statChanges;
        }

        public static SettlementLogItemResponse of(
            final String phase,
            final String description,
            final Long cashChange,
            final StatChangesResponse statChanges
        ) {
            return SettlementLogItemResponse.builder()
                .phase(phase)
                .description(description)
                .cashChange(cashChange)
                .statChanges(statChanges)
                .build();
        }
    }

    @Getter
    public static class UpdatedAssetsResponse {

        private final Long cash;
        private final Long loan;
        private final Long realEstateValue;
        private final Long netAssets;

        @Builder(access = AccessLevel.PRIVATE)
        private UpdatedAssetsResponse(
            final Long cash,
            final Long loan,
            final Long realEstateValue,
            final Long netAssets
        ) {
            if (cash == null || loan == null || realEstateValue == null || netAssets == null) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
            this.cash = cash;
            this.loan = loan;
            this.realEstateValue = realEstateValue;
            this.netAssets = netAssets;
        }

        public static UpdatedAssetsResponse of(
            final Long cash,
            final Long loan,
            final Long realEstateValue,
            final Long netAssets
        ) {
            return UpdatedAssetsResponse.builder()
                .cash(cash)
                .loan(loan)
                .realEstateValue(realEstateValue)
                .netAssets(netAssets)
                .build();
        }
    }

    @Getter
    public static class StatChangesResponse {

        private final Integer health;
        private final Integer fatigue;
        private final Integer stress;
        private final Integer happiness;
        private final Integer knowledge;

        @Builder(access = AccessLevel.PRIVATE)
        private StatChangesResponse(
            final Integer health,
            final Integer fatigue,
            final Integer stress,
            final Integer happiness,
            final Integer knowledge
        ) {
            if (health == null || fatigue == null || stress == null
                || happiness == null || knowledge == null) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
            this.health = health;
            this.fatigue = fatigue;
            this.stress = stress;
            this.happiness = happiness;
            this.knowledge = knowledge;
        }

        public static StatChangesResponse of(
            final Integer health,
            final Integer fatigue,
            final Integer stress,
            final Integer happiness,
            final Integer knowledge
        ) {
            return StatChangesResponse.builder()
                .health(health)
                .fatigue(fatigue)
                .stress(stress)
                .happiness(happiness)
                .knowledge(knowledge)
                .build();
        }

        public static StatChangesResponse from(final Map<String, Integer> stats) {
            if (stats == null) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
            return StatChangesResponse.of(
                stats.getOrDefault("health", 0),
                stats.getOrDefault("fatigue", 0),
                stats.getOrDefault("stress", 0),
                stats.getOrDefault("happiness", 0),
                stats.getOrDefault("knowledge", 0)
            );
        }

        public static StatChangesResponse zero() {
            return StatChangesResponse.of(0, 0, 0, 0, 0);
        }
    }

    public static class FlagsResponse {

        private final boolean isBankrupt;
        private final boolean isCleared;
        private final boolean isBurnout;
        private final boolean isForcedResignation;
        private final boolean hasEvent;

        @Builder(access = AccessLevel.PRIVATE)
        private FlagsResponse(
            final boolean isBankrupt,
            final boolean isCleared,
            final boolean isBurnout,
            final boolean isForcedResignation,
            final boolean hasEvent
        ) {
            this.isBankrupt = isBankrupt;
            this.isCleared = isCleared;
            this.isBurnout = isBurnout;
            this.isForcedResignation = isForcedResignation;
            this.hasEvent = hasEvent;
        }

        public static FlagsResponse of(
            final boolean isBankrupt,
            final boolean isCleared,
            final boolean isBurnout,
            final boolean isForcedResignation,
            final boolean hasEvent
        ) {
            return FlagsResponse.builder()
                .isBankrupt(isBankrupt)
                .isCleared(isCleared)
                .isBurnout(isBurnout)
                .isForcedResignation(isForcedResignation)
                .hasEvent(hasEvent)
                .build();
        }

        @JsonProperty("isBankrupt")
        public boolean isBankrupt() {
            return isBankrupt;
        }

        @JsonProperty("isCleared")
        public boolean isCleared() {
            return isCleared;
        }

        @JsonProperty("isBurnout")
        public boolean isBurnout() {
            return isBurnout;
        }

        @JsonProperty("isForcedResignation")
        public boolean isForcedResignation() {
            return isForcedResignation;
        }

        @JsonProperty("hasEvent")
        public boolean isHasEvent() {
            return hasEvent;
        }
    }
}
