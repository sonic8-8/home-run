package io.ssafy.p.j14c103.homerun.domain.world.housing;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ContractReviewPolicy {

    private static final String ALL_DEPOSIT_LOST = "ALL_DEPOSIT_LOST";
    private static final int CRITICAL_STRESS_THRESHOLD = 30;

    public ContractReviewCalculationResult calculate(
        final List<String> checkedTraps,
        final List<String> exposedChecklistTrapIds,
        final List<ContractTrap> actualTraps
    ) {
        validateInputs(checkedTraps, exposedChecklistTrapIds, actualTraps);

        final LinkedHashSet<String> normalizedCheckedTraps = normalizeCheckedTraps(
            checkedTraps,
            exposedChecklistTrapIds
        );
        final Map<String, ContractTrap> actualTrapMap = mapActualTraps(actualTraps);
        final LinkedHashSet<String> actualTrapIds = new LinkedHashSet<>(actualTrapMap.keySet());
        final LinkedHashSet<String> detectedTrapIds = calculateDetectedTrapIds(
            normalizedCheckedTraps,
            actualTrapIds
        );
        final boolean hasMissedCriticalTrap = hasMissedCriticalTrap(
            normalizedCheckedTraps,
            actualTrapMap
        );

        if (normalizedCheckedTraps.equals(actualTrapIds)) {
            return ContractReviewCalculationResult.of(
                List.copyOf(normalizedCheckedTraps),
                List.copyOf(detectedTrapIds),
                detectedTrapIds.size(),
                detectedTrapIds.size(),
                ContractResult.SAFE,
                ContractReviewStatus.PASSED,
                toMatchedActualTraps(detectedTrapIds, actualTrapMap)
            );
        }

        if (hasMissedCriticalTrap) {
            return ContractReviewCalculationResult.of(
                List.copyOf(normalizedCheckedTraps),
                List.copyOf(detectedTrapIds),
                detectedTrapIds.size(),
                detectedTrapIds.size(),
                ContractResult.FAIL,
                ContractReviewStatus.FAILED,
                toMatchedActualTraps(detectedTrapIds, actualTrapMap)
            );
        }

        return ContractReviewCalculationResult.of(
            List.copyOf(normalizedCheckedTraps),
            List.copyOf(detectedTrapIds),
            detectedTrapIds.size(),
            detectedTrapIds.size(),
            ContractResult.WARNING,
            ContractReviewStatus.PASSED,
            toMatchedActualTraps(detectedTrapIds, actualTrapMap)
        );
    }

    private void validateInputs(
        final List<String> checkedTraps,
        final List<String> exposedChecklistTrapIds,
        final List<ContractTrap> actualTraps
    ) {
        if (checkedTraps == null || exposedChecklistTrapIds == null || actualTraps == null) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private LinkedHashSet<String> normalizeCheckedTraps(
        final List<String> checkedTraps,
        final List<String> exposedChecklistTrapIds
    ) {
        final LinkedHashSet<String> exposedTrapUniverse = new LinkedHashSet<>();
        for (final String trapId : exposedChecklistTrapIds) {
            validateTrapId(trapId);
            exposedTrapUniverse.add(trapId);
        }

        final LinkedHashSet<String> normalizedCheckedTraps = new LinkedHashSet<>();
        for (final String checkedTrapId : checkedTraps) {
            validateTrapId(checkedTrapId);
            if (!exposedTrapUniverse.contains(checkedTrapId)) {
                throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
            }
            if (!normalizedCheckedTraps.add(checkedTrapId)) {
                throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
            }
        }
        return normalizedCheckedTraps;
    }

    private Map<String, ContractTrap> mapActualTraps(final List<ContractTrap> actualTraps) {
        final Map<String, ContractTrap> actualTrapMap = new LinkedHashMap<>();
        for (final ContractTrap actualTrap : actualTraps) {
            if (actualTrap == null) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
            validateTrapId(actualTrap.getTrapId());
            actualTrapMap.put(actualTrap.getTrapId(), actualTrap);
        }
        return actualTrapMap;
    }

    private LinkedHashSet<String> calculateDetectedTrapIds(
        final LinkedHashSet<String> checkedTraps,
        final LinkedHashSet<String> actualTrapIds
    ) {
        final LinkedHashSet<String> detectedTrapIds = new LinkedHashSet<>();
        for (final String checkedTrap : checkedTraps) {
            if (actualTrapIds.contains(checkedTrap)) {
                detectedTrapIds.add(checkedTrap);
            }
        }
        return detectedTrapIds;
    }

    private boolean hasMissedCriticalTrap(
        final LinkedHashSet<String> checkedTraps,
        final Map<String, ContractTrap> actualTrapMap
    ) {
        for (final ContractTrap actualTrap : actualTrapMap.values()) {
            if (checkedTraps.contains(actualTrap.getTrapId())) {
                continue;
            }
            if (isCritical(actualTrap)) {
                return true;
            }
        }
        return false;
    }

    private boolean isCritical(final ContractTrap actualTrap) {
        final ContractTrapPenalty penalty = actualTrap.getPenalty();
        if (penalty == null) {
            return false;
        }
        if (ALL_DEPOSIT_LOST.equals(penalty.getCash())) {
            return true;
        }
        return penalty.getStress() != null && penalty.getStress() >= CRITICAL_STRESS_THRESHOLD;
    }

    private List<ContractTrap> toMatchedActualTraps(
        final LinkedHashSet<String> detectedTrapIds,
        final Map<String, ContractTrap> actualTrapMap
    ) {
        return detectedTrapIds.stream()
            .map(actualTrapMap::get)
            .toList();
    }

    private void validateTrapId(final String trapId) {
        if (trapId == null || trapId.isBlank()) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
