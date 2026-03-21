package io.ssafy.p.j14c103.homerun.domain.character.schedule;

import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import java.util.Set;

public class TurnSlotPreviewPolicy {

    private static final int SLOT_COUNT = 3;
    private static final Set<Integer> VALID_SLOT_INDICES = Set.of(0, 1, 2);

    private final ActionResolutionPolicy actionResolutionPolicy;

    public TurnSlotPreviewPolicy() {
        this(new ActionResolutionPolicy());
    }

    TurnSlotPreviewPolicy(final ActionResolutionPolicy actionResolutionPolicy) {
        if (actionResolutionPolicy == null) {
            throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
        }

        this.actionResolutionPolicy = actionResolutionPolicy;
    }

    public PreviewResult preview(
        final GameStat gameStat,
        final List<RequestedSlot> requestedSlots
    ) {
        validateGameStat(gameStat);
        validateRequestedSlots(requestedSlots);

        final int knowledge = requireKnowledge(gameStat.getKnowledge());
        final List<RequestedSlot> normalizedSlots = normalizeSlots(requestedSlots);
        final boolean burnout = Boolean.TRUE.equals(gameStat.getBurnout());
        final List<PreviewSlot> previewSlots = resolvePreviewSlots(
            normalizedSlots,
            burnout
        );

        return aggregatePreview(previewSlots, knowledge, burnout);
    }

    private void validateGameStat(final GameStat gameStat) {
        if (gameStat == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
    }

    private void validateRequestedSlots(final List<RequestedSlot> requestedSlots) {
        if (requestedSlots == null || requestedSlots.size() != SLOT_COUNT) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
        if (requestedSlots.stream().anyMatch(java.util.Objects::isNull)) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
    }

    private int requireKnowledge(final Integer knowledge) {
        if (knowledge == null) {
            throw new HomerunException(ErrorCode.CHARACTER_STATE_UNINITIALIZED);
        }

        return knowledge;
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
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }

        if (!normalizedSlots.stream()
            .map(RequestedSlot::slotIndex)
            .allMatch(VALID_SLOT_INDICES::contains)) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
    }

    private List<PreviewSlot> resolvePreviewSlots(
        final List<RequestedSlot> normalizedSlots,
        final boolean burnout
    ) {
        if (!burnout) {
            return normalizedSlots.stream()
                .map(requestedSlot -> PreviewSlot.of(
                    requestedSlot.slotIndex(),
                    requestedSlot.actionType(),
                    false
                ))
                .toList();
        }

        final long nonRestSlotCount = normalizedSlots.stream()
            .filter(requestedSlot -> requestedSlot.actionType() != ActionType.REST)
            .count();
        if (nonRestSlotCount > 1) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }

        final long restSlotCount = normalizedSlots.stream()
            .filter(requestedSlot -> requestedSlot.actionType() == ActionType.REST)
            .count();
        if (restSlotCount < 2) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
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
        final List<PreviewSlot> previewSlots,
        final int knowledge,
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
                    actionResolutionPolicy.getPreviewAction(previewSlot.actionType(), knowledge);

                return previewSlot.resolve(resolvedAction.category(), resolvedAction.statDelta(),
                    resolvedAction.cashPreview());
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
            minimumCashDelta += cashPreview.minimumCashDelta();
            maximumCashDelta += cashPreview.maximumCashDelta();
            rangePreview = rangePreview || cashPreview.isRange();
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
                throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
            }
            if (actionType == null) {
                throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
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
                throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
            }
            if (statPreview == null) {
                throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
            }
            if (cashPreview == null) {
                throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
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
                throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
            }
            if (actionType == null) {
                throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
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
            if (actionCategory == null) {
                throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
            }
            if (statDelta == null) {
                throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
            }
            if (cashPreview == null) {
                throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
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
                throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
            }
            if (!rangePreview && minimumCashDelta != maximumCashDelta) {
                throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
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
