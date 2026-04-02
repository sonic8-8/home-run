package io.ssafy.p.j14c103.homerun.api.service.world;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class GameWorldRollService {

    private static final int MIN_ROLL = 1;
    private static final int MAX_ROLL = 100;
    private static final int PROBABILITY_SCALE = 10_000;

    public int resolveTurnRoll(final Long gameSessionId, final int turnNumber) {
        validateGameSessionId(gameSessionId);
        validatePositive(turnNumber);

        return toRoll(Objects.hash(gameSessionId, turnNumber, "turn-world-roll"));
    }

    public int deriveRoll(
        final Long gameSessionId,
        final int baseRoll,
        final String scope
    ) {
        validateGameSessionId(gameSessionId);
        validateRoll(baseRoll);
        validateScope(scope);

        return toRoll(Objects.hash(gameSessionId, baseRoll, scope));
    }

    public BigDecimal deriveProbabilityRoll(
        final Long gameSessionId,
        final int baseRoll,
        final String scope
    ) {
        validateGameSessionId(gameSessionId);
        validateRoll(baseRoll);
        validateScope(scope);

        final int bucket = Math.floorMod(Objects.hash(gameSessionId, baseRoll, scope), PROBABILITY_SCALE);
        return BigDecimal.valueOf(bucket, 4);
    }

    public int selectIndex(
        final Long gameSessionId,
        final int baseRoll,
        final String scope,
        final int size
    ) {
        validateGameSessionId(gameSessionId);
        validateRoll(baseRoll);
        validateScope(scope);
        if (size <= 0) {
            throw new HomerunException(ErrorCode.WORLD_RESULT_INVALID);
        }

        return Math.floorMod(Objects.hash(gameSessionId, baseRoll, scope), size);
    }

    private int toRoll(final int seed) {
        return Math.floorMod(seed, MAX_ROLL) + MIN_ROLL;
    }

    private void validateGameSessionId(final Long gameSessionId) {
        if (gameSessionId == null || gameSessionId <= 0L) {
            throw new HomerunException(ErrorCode.WORLD_RESULT_INVALID);
        }
    }

    private void validatePositive(final int value) {
        if (value <= 0) {
            throw new HomerunException(ErrorCode.WORLD_RESULT_INVALID);
        }
    }

    private void validateRoll(final int roll) {
        if (roll < MIN_ROLL || roll > MAX_ROLL) {
            throw new HomerunException(ErrorCode.WORLD_RESULT_INVALID);
        }
    }

    private void validateScope(final String scope) {
        if (scope == null || scope.isBlank()) {
            throw new HomerunException(ErrorCode.WORLD_RESULT_INVALID);
        }
    }
}
