package io.ssafy.p.j14c103.homerun.api.service.pass.response;

import java.math.BigDecimal;

public class PassWidgetResponse {

    private final BigDecimal todaySaving;
    private final BigDecimal weekSaving;
    private final BigDecimal goalAmount;
    private final double achievementRate;

    private PassWidgetResponse(
            final BigDecimal todaySaving,
            final BigDecimal weekSaving,
            final BigDecimal goalAmount,
            final double achievementRate) {
        this.todaySaving = todaySaving;
        this.weekSaving = weekSaving;
        this.goalAmount = goalAmount;
        this.achievementRate = achievementRate;
    }

    public static PassWidgetResponse of(
            final BigDecimal todaySaving,
            final BigDecimal weekSaving,
            final BigDecimal goalAmount) {
        final double rate = goalAmount.compareTo(BigDecimal.ZERO) > 0
                ? weekSaving.doubleValue() / goalAmount.doubleValue() * 100.0
                : 0.0;
        return new PassWidgetResponse(todaySaving, weekSaving, goalAmount, Math.min(rate, 100.0));
    }

    public BigDecimal getTodaySaving() {
        return todaySaving;
    }

    public BigDecimal getWeekSaving() {
        return weekSaving;
    }

    public BigDecimal getGoalAmount() {
        return goalAmount;
    }

    public double getAchievementRate() {
        return achievementRate;
    }
}
