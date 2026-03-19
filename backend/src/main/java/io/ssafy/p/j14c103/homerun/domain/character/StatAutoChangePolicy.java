package io.ssafy.p.j14c103.homerun.domain.character;

import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;

public class StatAutoChangePolicy {

    private static final int HEALTH_DELTA = -2;
    private static final int HAPPINESS_DELTA = 0;
    private static final int HIGH_KNOWLEDGE_THRESHOLD = 61;
    private static final int MID_KNOWLEDGE_THRESHOLD = 31;

    public StatAutoChange calculate(final GameStat gameStat, final HousingType housingType) {
        validateNotNull(gameStat, "gameStat");
        validateNotNull(housingType, "housingType");

        return new StatAutoChange(
            HEALTH_DELTA,
            resolveFatigueDelta(housingType),
            resolveStressDelta(
                requireStat("happiness", gameStat.getHappiness()),
                housingType
            ),
            HAPPINESS_DELTA,
            resolveKnowledgeDelta(requireStat("knowledge", gameStat.getKnowledge()))
        );
    }

    private int resolveKnowledgeDelta(final int knowledge) {
        if (knowledge >= HIGH_KNOWLEDGE_THRESHOLD) {
            return -3;
        }

        if (knowledge >= MID_KNOWLEDGE_THRESHOLD) {
            return -2;
        }

        return -1;
    }

    private int resolveStressDelta(final int happiness, final HousingType housingType) {
        return resolveHousingStressDelta(housingType) + resolveStressReliefDelta(happiness);
    }

    private int resolveStressReliefDelta(final int happiness) {
        return -(happiness / 25);
    }

    private int resolveHousingStressDelta(final HousingType housingType) {
        if (housingType == HousingType.NONE) {
            return 15;
        }

        if (housingType == HousingType.STUDIO) {
            return 5;
        }

        if (housingType == HousingType.VILLA) {
            return 0;
        }

        if (housingType == HousingType.JEONSE_APT) {
            return -3;
        }

        if (housingType == HousingType.OWNED_APT) {
            return -6;
        }

        throw new HomerunException(ErrorCode.CHARACTER_HOUSING_TYPE_UNSUPPORTED);
    }

    private int resolveFatigueDelta(final HousingType housingType) {
        if (housingType == HousingType.NONE) {
            return -4;
        }

        if (housingType == HousingType.STUDIO) {
            return -5;
        }

        if (housingType == HousingType.VILLA) {
            return -8;
        }

        if (housingType == HousingType.JEONSE_APT) {
            return -11;
        }

        if (housingType == HousingType.OWNED_APT) {
            return -13;
        }

        throw new HomerunException(ErrorCode.CHARACTER_HOUSING_TYPE_UNSUPPORTED);
    }

    private int requireStat(final String fieldName, final Integer value) {
        if (value == null) {
            throw new HomerunException(ErrorCode.CHARACTER_STATE_UNINITIALIZED);
        }

        return value;
    }

    private void validateNotNull(final Object value, final String fieldName) {
        if (value == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
    }

    public record StatAutoChange(
        int healthDelta,
        int fatigueDelta,
        int stressDelta,
        int happinessDelta,
        int knowledgeDelta
    ) {
    }
}
