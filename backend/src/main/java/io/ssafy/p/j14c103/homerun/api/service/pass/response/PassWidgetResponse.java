package io.ssafy.p.j14c103.homerun.api.service.pass.response;

public class PassWidgetResponse {

    private final int todaySaved;
    private final int weeklySaved;
    private final int weeklyGoal;
    private final int progressRate;
    private final int remaining;

    private PassWidgetResponse(final int todaySaved, final int weeklySaved,
                               final int weeklyGoal, final int progressRate, final int remaining) {
        this.todaySaved = todaySaved;
        this.weeklySaved = weeklySaved;
        this.weeklyGoal = weeklyGoal;
        this.progressRate = progressRate;
        this.remaining = remaining;
    }

    public static PassWidgetResponse of(final int todaySaved, final int weeklySaved, final int weeklyGoal) {
        final int rate = weeklyGoal > 0
                ? (int) Math.min((double) weeklySaved / weeklyGoal * 100.0, 100.0)
                : 0;
        final int remaining = Math.max(weeklyGoal - weeklySaved, 0);
        return new PassWidgetResponse(todaySaved, weeklySaved, weeklyGoal, rate, remaining);
    }

    public int getTodaySaved() { return todaySaved; }
    public int getWeeklySaved() { return weeklySaved; }
    public int getWeeklyGoal() { return weeklyGoal; }
    public int getProgressRate() { return progressRate; }
    public int getRemaining() { return remaining; }
}
