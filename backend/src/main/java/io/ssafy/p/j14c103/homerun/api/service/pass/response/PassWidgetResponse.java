package io.ssafy.p.j14c103.homerun.api.service.pass.response;

public class PassWidgetResponse {

    private final long todaySaved;
    private final long weeklySaved;
    private final long weeklyGoal;
    private final int progressRate;
    private final long remaining;

    private PassWidgetResponse(final long todaySaved, final long weeklySaved,
                               final long weeklyGoal, final int progressRate, final long remaining) {
        this.todaySaved = todaySaved;
        this.weeklySaved = weeklySaved;
        this.weeklyGoal = weeklyGoal;
        this.progressRate = progressRate;
        this.remaining = remaining;
    }

    public static PassWidgetResponse of(final long todaySaved, final long weeklySaved, final long weeklyGoal) {
        final int rate = weeklyGoal > 0
                ? (int) Math.min((double) weeklySaved / weeklyGoal * 100.0, 100.0)
                : 0;
        final long remaining = Math.max(weeklyGoal - weeklySaved, 0L);
        return new PassWidgetResponse(todaySaved, weeklySaved, weeklyGoal, rate, remaining);
    }

    public long getTodaySaved() { return todaySaved; }
    public long getWeeklySaved() { return weeklySaved; }
    public long getWeeklyGoal() { return weeklyGoal; }
    public int getProgressRate() { return progressRate; }
    public long getRemaining() { return remaining; }
}
