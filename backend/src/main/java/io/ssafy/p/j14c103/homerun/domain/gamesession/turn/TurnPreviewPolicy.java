package io.ssafy.p.j14c103.homerun.domain.gamesession.turn;

import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TurnPreviewPolicy {

    private static final int SLOT_COUNT = 3;
    private static final Set<Integer> VALID_SLOT_INDICES = Set.of(0, 1, 2);

    private final ActionResolutionPolicy actionResolutionPolicy;

    public PreviewResult preview(
        final GameStat gameStat,
        final int currentTurn,
        final List<RequestedSlot> requestedSlots
    ) {
        validateGameStat(gameStat);
        validateRequestedSlots(requestedSlots);

        final List<RequestedSlot> normalizedSlots = normalizeSlots(requestedSlots);
        final boolean hospitalized = gameStat.isHospitalizedAt(currentTurn);
        final boolean burnout = Boolean.TRUE.equals(gameStat.getBurnout()) && !hospitalized;
        final List<PreviewSlot> previewSlots = resolvePreviewSlots(
            normalizedSlots,
            hospitalized,
            burnout
        );

        return aggregatePreview(gameStat, currentTurn, previewSlots, burnout);
    }

    private void validateGameStat(final GameStat gameStat) {
        if (gameStat == null) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void validateRequestedSlots(final List<RequestedSlot> requestedSlots) {
        if (requestedSlots == null || requestedSlots.size() != SLOT_COUNT) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (requestedSlots.stream().anyMatch(java.util.Objects::isNull)) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private List<RequestedSlot> normalizeSlots(final List<RequestedSlot> requestedSlots) {
        final List<RequestedSlot> normalizedSlots = requestedSlots.stream()
            .sorted((left, right) -> Integer.compare(left.slotIndex(), right.slotIndex()))
            .toList();

        validateSlotIndices(normalizedSlots);
        return normalizedSlots;
    }

    private void validateSlotIndices(final List<RequestedSlot> normalizedSlots) {
        if (normalizedSlots.stream()
            .map(RequestedSlot::slotIndex)
            .collect(java.util.stream.Collectors.toSet())
            .size() != SLOT_COUNT) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (!normalizedSlots.stream()
            .map(RequestedSlot::slotIndex)
            .allMatch(VALID_SLOT_INDICES::contains)) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private List<PreviewSlot> resolvePreviewSlots(
        final List<RequestedSlot> normalizedSlots,
        final boolean hospitalized,
        final boolean burnout
    ) {
        if (hospitalized) {
            validateHospitalizedSlots(normalizedSlots);
            return normalizedSlots.stream()
                .map(requestedSlot -> PreviewSlot.of(
                    requestedSlot.slotIndex(),
                    requestedSlot.actionType(),
                    true
                ))
                .toList();
        }

        if (!burnout) {
            return normalizedSlots.stream()
                .map(requestedSlot -> PreviewSlot.of(
                    requestedSlot.slotIndex(),
                    requestedSlot.actionType(),
                    false
                ))
                .toList();
        }

        return resolveBurnoutSlots(normalizedSlots);
    }

    private void validateHospitalizedSlots(final List<RequestedSlot> normalizedSlots) {
        final boolean hasNonRestAction = normalizedSlots.stream()
            .anyMatch(requestedSlot -> requestedSlot.actionType() != ActionType.REST);

        if (hasNonRestAction) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private List<PreviewSlot> resolveBurnoutSlots(final List<RequestedSlot> normalizedSlots) {
        final long nonRestSlotCount = normalizedSlots.stream()
            .filter(requestedSlot -> requestedSlot.actionType() != ActionType.REST)
            .count();
        if (nonRestSlotCount > 1) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }

        final long restSlotCount = normalizedSlots.stream()
            .filter(requestedSlot -> requestedSlot.actionType() == ActionType.REST)
            .count();
        if (restSlotCount < 2) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (nonRestSlotCount == 0) {
            final int playerControlledSlotIndex = normalizedSlots.get(0).slotIndex();

            return normalizedSlots.stream()
                .map(requestedSlot -> PreviewSlot.of(
                    requestedSlot.slotIndex(),
                    requestedSlot.actionType(),
                    requestedSlot.slotIndex() != playerControlledSlotIndex
                ))
                .toList();
        }

        return normalizedSlots.stream()
            .map(requestedSlot -> PreviewSlot.of(
                requestedSlot.slotIndex(),
                requestedSlot.actionType(),
                requestedSlot.actionType() == ActionType.REST
            ))
            .toList();
    }

    private PreviewResult aggregatePreview(
        final GameStat gameStat,
        final int currentTurn,
        final List<PreviewSlot> previewSlots,
        final boolean burnout
    ) {
        int healthDelta = 0;
        int fatigueDelta = 0;
        int stressDelta = 0;
        int happinessDelta = 0;
        int knowledgeDelta = 0;
        int minimumCashDelta = 0;
        int maximumCashDelta = 0;
        boolean rangePreview = false;

        final List<PreviewSlot> resolvedSlots = previewSlots.stream()
            .map(previewSlot -> {
                final ActionResolutionPolicy.ResolvedAction resolvedAction =
                    actionResolutionPolicy.resolvePreviewAction(
                        previewSlot.actionType(),
                        gameStat,
                        currentTurn
                    );

                if (resolvedAction.disabled()) {
                    throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
                }

                return previewSlot.resolve(
                    resolvedAction.category(),
                    resolvedAction.statDelta(),
                    resolvedAction.cashPreview()
                );
            })
            .toList();

        for (PreviewSlot resolvedSlot : resolvedSlots) {
            final ActionCatalog.ActionStatDelta statDelta = resolvedSlot.statDelta();
            final ActionResolutionPolicy.CashPreview cashPreview = resolvedSlot.cashPreview();

            healthDelta += statDelta.healthDelta();
            fatigueDelta += statDelta.fatigueDelta();
            stressDelta += statDelta.stressDelta();
            happinessDelta += statDelta.happinessDelta();
            if (!burnout) {
                knowledgeDelta += statDelta.knowledgeDelta();
            }
            minimumCashDelta += cashPreview.minAmount();
            maximumCashDelta += cashPreview.maxAmount();
            rangePreview = rangePreview || cashPreview.rangePreview();
        }

        return PreviewResult.of(
            resolvedSlots,
            StatPreview.of(
                healthDelta,
                fatigueDelta,
                stressDelta,
                happinessDelta,
                knowledgeDelta
            ),
            CashPreview.of(minimumCashDelta, maximumCashDelta, rangePreview)
        );
    }

    public record RequestedSlot(
        int slotIndex,
        ActionType actionType
    ) {

        public RequestedSlot {
            if (!VALID_SLOT_INDICES.contains(slotIndex)) {
                throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
            }
            if (actionType == null) {
                throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
            }
        }

        public static RequestedSlot of(final int slotIndex, final ActionType actionType) {
            return new RequestedSlot(slotIndex, actionType);
        }
    }

    public record PreviewResult(
        List<PreviewSlot> slots,
        StatPreview statPreview,
        CashPreview cashPreview
    ) {

        public PreviewResult {
            if (slots == null || slots.size() != SLOT_COUNT) {
                throw new HomerunException(ErrorCode.SCHEDULE_ACTION_CATALOG_INVALID);
            }
            if (statPreview == null || cashPreview == null) {
                throw new HomerunException(ErrorCode.SCHEDULE_ACTION_CATALOG_INVALID);
            }
            slots = List.copyOf(slots);
        }

        public static PreviewResult of(
            final List<PreviewSlot> slots,
            final StatPreview statPreview,
            final CashPreview cashPreview
        ) {
            return new PreviewResult(slots, statPreview, cashPreview);
        }
    }

    public record PreviewSlot(
        int slotIndex,
        ActionType actionType,
        ActionCategory actionCategory,
        boolean forcedAction,
        ActionCatalog.ActionStatDelta statDelta,
        ActionResolutionPolicy.CashPreview cashPreview
    ) {

        public PreviewSlot {
            if (!VALID_SLOT_INDICES.contains(slotIndex)) {
                throw new HomerunException(ErrorCode.SCHEDULE_ACTION_CATALOG_INVALID);
            }
            if (actionType == null) {
                throw new HomerunException(ErrorCode.SCHEDULE_ACTION_CATALOG_INVALID);
            }
        }

        public static PreviewSlot of(
            final int slotIndex,
            final ActionType actionType,
            final boolean forcedAction
        ) {
            return new PreviewSlot(slotIndex, actionType, null, forcedAction, null, null);
        }

        private PreviewSlot resolve(
            final ActionCategory actionCategory,
            final ActionCatalog.ActionStatDelta statDelta,
            final ActionResolutionPolicy.CashPreview cashPreview
        ) {
            if (actionCategory == null || statDelta == null || cashPreview == null) {
                throw new HomerunException(ErrorCode.SCHEDULE_ACTION_CATALOG_INVALID);
            }

            return new PreviewSlot(
                slotIndex,
                actionType,
                actionCategory,
                forcedAction,
                statDelta,
                cashPreview
            );
        }
    }

    public record StatPreview(
        int healthDelta,
        int fatigueDelta,
        int stressDelta,
        int happinessDelta,
        int knowledgeDelta
    ) {

        public static StatPreview of(
            final int healthDelta,
            final int fatigueDelta,
            final int stressDelta,
            final int happinessDelta,
            final int knowledgeDelta
        ) {
            return new StatPreview(
                healthDelta,
                fatigueDelta,
                stressDelta,
                happinessDelta,
                knowledgeDelta
            );
        }
    }

    public record CashPreview(
        int minimumCashDelta,
        int maximumCashDelta,
        boolean rangePreview
    ) {

        public CashPreview {
            if (minimumCashDelta > maximumCashDelta) {
                throw new HomerunException(ErrorCode.SCHEDULE_ACTION_CATALOG_INVALID);
            }
            if (!rangePreview && minimumCashDelta != maximumCashDelta) {
                throw new HomerunException(ErrorCode.SCHEDULE_ACTION_CATALOG_INVALID);
            }
        }

        public static CashPreview of(
            final int minimumCashDelta,
            final int maximumCashDelta,
            final boolean rangePreview
        ) {
            return new CashPreview(minimumCashDelta, maximumCashDelta, rangePreview);
        }
    }
}
