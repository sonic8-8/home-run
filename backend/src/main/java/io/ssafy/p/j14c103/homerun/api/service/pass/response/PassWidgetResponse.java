package io.ssafy.p.j14c103.homerun.api.service.pass.response;

public class PassWidgetResponse {

    private final int todaySaving;
    private final int weekSaving;
    private final int goalAmount;
    private final double achievementRate;

    private PassWidgetResponse(final int todaySaving, final int weekSaving,
                               final int goalAmount, final double achievementRate) {
        this.todaySaving = todaySaving;
        this.weekSaving = weekSaving;
        this.goalAmount = goalAmount;
        this.achievementRate = achievementRate;
    }

    public static PassWidgetResponse of(final int todaySaving, final int weekSaving, final int goalAmount) {
        final double rate = goalAmount > 0
                ? (double) weekSaving / goalAmount * 100.0
                : 0.0;
        return new PassWidgetResponse(todaySaving, weekSaving, goalAmount, Math.min(rate, 100.0));
    }

    public int getTodaySaving() { return todaySaving; }
    public int getWeekSaving() { return weekSaving; }
    public int getGoalAmount() { return goalAmount; }
    public double getAchievementRate() { return achievementRate; }
}
