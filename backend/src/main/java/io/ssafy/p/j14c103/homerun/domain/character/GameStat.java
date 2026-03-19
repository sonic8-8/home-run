package io.ssafy.p.j14c103.homerun.domain.character;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "게임스탯")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class GameStat {

    private static final int MIN_STAT = 0;
    private static final int MAX_STAT = 100;
    private static final int BURNOUT_ENTRY_THRESHOLD = 80;
    private static final int BURNOUT_RELEASE_THRESHOLD = 50;
    private static final int FORCED_RESIGNATION_HEALTH_THRESHOLD = 9;
    private static final int HOSPITALIZATION_HEALTH_THRESHOLD = 29;
    private static final int MEDICAL_BILL_HEALTH_THRESHOLD = 49;
    private static final int NEGOTIATION_PENALTY_HEALTH_THRESHOLD = 69;

    @Id
    @Column(name = "게임번호", nullable = false)
    private Integer gameId;

    @Column(name = "체력", nullable = false)
    private Integer health;

    @Column(name = "피로도", nullable = false)
    private Integer fatigue;

    @Column(name = "스트레스", nullable = false)
    private Integer stress;

    @Column(name = "행복도", nullable = false)
    private Integer happiness;

    @Column(name = "지식")
    private Integer knowledge;

    @Column(name = "번아웃여부")
    private Boolean burnout;

    @Column(name = "번아웃시작턴")
    private Integer burnoutStartedTurn;

    @Column(name = "입원종료턴")
    private Integer hospitalizedUntilTurn;

    public static GameStat create(
        final Integer gameId,
        final int health,
        final int fatigue,
        final int stress,
        final int happiness,
        final int knowledge,
        final int currentTurn
    ) {
        validateGameId(gameId);
        validateTurn(currentTurn, "currentTurn");
        validateStatRange("health", health);
        validateStatRange("fatigue", fatigue);
        validateStatRange("stress", stress);
        validateStatRange("happiness", happiness);
        validateStatRange("knowledge", knowledge);

        final boolean burnout = isBurnoutThresholdMet(fatigue, stress);
        return GameStat.builder()
            .gameId(gameId)
            .health(health)
            .fatigue(fatigue)
            .stress(stress)
            .happiness(happiness)
            .knowledge(knowledge)
            .burnout(burnout)
            .burnoutStartedTurn(burnout ? currentTurn : null)
            .hospitalizedUntilTurn(null)
            .build();
    }

    public void applyChange(
        final int healthDelta,
        final int fatigueDelta,
        final int stressDelta,
        final int happinessDelta,
        final int knowledgeDelta,
        final int currentTurn
    ) {
        validateTurn(currentTurn, "currentTurn");

        this.health = clampStat(requireInitialized("health", health) + healthDelta);
        this.fatigue = clampStat(requireInitialized("fatigue", fatigue) + fatigueDelta);
        this.stress = clampStat(requireInitialized("stress", stress) + stressDelta);
        this.happiness = clampStat(requireInitialized("happiness", happiness) + happinessDelta);
        this.knowledge = clampStat(requireInitialized("knowledge", knowledge) + knowledgeDelta);

        refreshBurnout(currentTurn);
    }

    public boolean isForcedResignationRisk() {
        return evaluateHealthRisk().isForcedResignationCandidate();
    }

    public HealthRisk evaluateHealthRisk() {
        final int currentHealth = requireInitialized("health", health);

        if (currentHealth <= FORCED_RESIGNATION_HEALTH_THRESHOLD) {
            return HealthRisk.FORCED_RESIGNATION_CANDIDATE;
        }

        if (currentHealth <= HOSPITALIZATION_HEALTH_THRESHOLD) {
            return HealthRisk.HOSPITALIZATION_CANDIDATE;
        }

        if (currentHealth <= MEDICAL_BILL_HEALTH_THRESHOLD) {
            return HealthRisk.MEDICAL_BILL_CANDIDATE;
        }

        if (currentHealth <= NEGOTIATION_PENALTY_HEALTH_THRESHOLD) {
            return HealthRisk.NEGOTIATION_PENALTY;
        }

        return HealthRisk.STABLE;
    }

    public void hospitalizeUntil(final int endTurn) {
        validateTurn(endTurn, "endTurn");
        this.hospitalizedUntilTurn = endTurn;
    }

    public boolean isHospitalizedAt(final int currentTurn) {
        validateTurn(currentTurn, "currentTurn");

        if (hospitalizedUntilTurn == null) {
            return false;
        }

        return currentTurn <= hospitalizedUntilTurn;
    }

    private void refreshBurnout(final int currentTurn) {
        final int currentFatigue = requireInitialized("fatigue", fatigue);
        final int currentStress = requireInitialized("stress", stress);

        if (Boolean.TRUE.equals(burnout)) {
            if (isBurnoutReleaseConditionMet(currentFatigue, currentStress)) {
                releaseBurnout();
            }
            return;
        }

        if (isBurnoutEntryConditionMet(currentFatigue, currentStress)) {
            enterBurnout(currentTurn);
        }
    }

    private void enterBurnout(final int currentTurn) {
        this.burnout = true;
        this.burnoutStartedTurn = currentTurn;
    }

    private void releaseBurnout() {
        this.burnout = false;
        this.burnoutStartedTurn = null;
    }

    private static boolean isBurnoutEntryConditionMet(final int fatigue, final int stress) {
        return fatigue >= BURNOUT_ENTRY_THRESHOLD && stress >= BURNOUT_ENTRY_THRESHOLD;
    }

    private static boolean isBurnoutReleaseConditionMet(final int fatigue, final int stress) {
        return fatigue <= BURNOUT_RELEASE_THRESHOLD && stress <= BURNOUT_RELEASE_THRESHOLD;
    }

    private static boolean isBurnoutThresholdMet(final int fatigue, final int stress) {
        return isBurnoutEntryConditionMet(fatigue, stress);
    }

    private static void validateGameId(final Integer gameId) {
        if (gameId == null || gameId <= 0) {
            throw new IllegalArgumentException("gameId는 1 이상이어야 합니다.");
        }
    }

    private static void validateTurn(final int turn, final String fieldName) {
        if (turn < 0) {
            throw new IllegalArgumentException(fieldName + "은 0 이상이어야 합니다.");
        }
    }

    private static void validateStatRange(final String fieldName, final int value) {
        if (value < MIN_STAT || value > MAX_STAT) {
            throw new IllegalArgumentException(fieldName + "는 0에서 100 사이여야 합니다.");
        }
    }

    private static int clampStat(final int value) {
        if (value < MIN_STAT) {
            return MIN_STAT;
        }

        if (value > MAX_STAT) {
            return MAX_STAT;
        }

        return value;
    }

    private static int requireInitialized(final String fieldName, final Integer value) {
        if (value == null) {
            throw new IllegalStateException(fieldName + "가 초기화되지 않았습니다.");
        }

        return value;
    }
}
