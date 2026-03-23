package io.ssafy.p.j14c103.homerun.domain.character.career;

import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;

public class ForcedResignationPolicy {

    private static final int MIN_KNOWLEDGE = 0;
    private static final int MAX_KNOWLEDGE = 100;
    private static final int MIN_TURN = 1;
    private static final int MAX_REHIRE_WAIT_TURNS = 3;
    private static final int MIN_REHIRE_WAIT_TURNS = 1;
    private static final int KNOWLEDGE_SEGMENT = 30;

    public ForcedResignationResult apply(
        final GameCareer gameCareer,
        final GameStat gameStat,
        final int currentTurn
    ) {
        validateRequest(gameCareer, gameStat, currentTurn);
        validateForcedResignationRisk(gameStat);
        validateEmploymentStatus(gameCareer.getEmploymentStatus());

        final int knowledge = requireKnowledge(gameStat.getKnowledge());
        final int previousSalary = requirePositiveSalary(gameCareer.getSalary());
        final int rehireWaitTurns = calculateRehireWaitTurns(knowledge);

        return ForcedResignationResult.of(
            previousSalary,
            rehireWaitTurns,
            currentTurn + rehireWaitTurns
        );
    }

    private void validateRequest(
        final GameCareer gameCareer,
        final GameStat gameStat,
        final int currentTurn
    ) {
        if (gameCareer == null || gameStat == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
        if (currentTurn < MIN_TURN) {
            throw new HomerunException(ErrorCode.CHARACTER_TURN_INVALID);
        }
    }

    private void validateForcedResignationRisk(final GameStat gameStat) {
        if (!gameStat.isForcedResignationRisk()) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
    }

    private void validateEmploymentStatus(final EmploymentStatus employmentStatus) {
        if (employmentStatus == null) {
            throw new HomerunException(ErrorCode.CHARACTER_STATE_UNINITIALIZED);
        }
        if (employmentStatus == EmploymentStatus.UNEMPLOYED) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
    }

    private int requireKnowledge(final Integer knowledge) {
        if (knowledge == null) {
            throw new HomerunException(ErrorCode.CHARACTER_STATE_UNINITIALIZED);
        }
        if (knowledge < MIN_KNOWLEDGE || knowledge > MAX_KNOWLEDGE) {
            throw new HomerunException(ErrorCode.CHARACTER_STAT_INVALID);
        }

        return knowledge;
    }

    private int requirePositiveSalary(final Integer salary) {
        if (salary == null) {
            throw new HomerunException(ErrorCode.CHARACTER_STATE_UNINITIALIZED);
        }
        if (salary <= 0) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }

        return salary;
    }

    private int calculateRehireWaitTurns(final int knowledge) {
        return Math.max(
            MIN_REHIRE_WAIT_TURNS,
            MAX_REHIRE_WAIT_TURNS - (knowledge / KNOWLEDGE_SEGMENT)
        );
    }

    public record ForcedResignationResult(
        int previousSalary,
        int rehireWaitTurns,
        int rehireAvailableTurn
    ) {

        public ForcedResignationResult {
            validatePositive(previousSalary);
            validatePositive(rehireWaitTurns);
            validatePositive(rehireAvailableTurn);
        }

        public static ForcedResignationResult of(
            final int previousSalary,
            final int rehireWaitTurns,
            final int rehireAvailableTurn
        ) {
            return new ForcedResignationResult(
                previousSalary,
                rehireWaitTurns,
                rehireAvailableTurn
            );
        }

        private static void validatePositive(final int value) {
            if (value < 1) {
                throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
            }
        }
    }
}
