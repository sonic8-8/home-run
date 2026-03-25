package io.ssafy.p.j14c103.homerun.api.service.pass.response;

import io.ssafy.p.j14c103.homerun.domain.pass.PassSubscription;

import java.time.LocalDateTime;
import java.util.List;

public class PassSubscriptionResponse {

    private final Long subscriptionId;
    private final Long passId;
    private final String name;
    private final Integer amountPerSave;
    private final Integer totalSaved;
    private final List<Boolean> weeklyHistory;
    private final LocalDateTime subscribedAt;

    private PassSubscriptionResponse(
            final Long subscriptionId, final Long passId, final String name,
            final Integer amountPerSave, final Integer totalSaved,
            final List<Boolean> weeklyHistory, final LocalDateTime subscribedAt) {
        this.subscriptionId = subscriptionId;
        this.passId = passId;
        this.name = name;
        this.amountPerSave = amountPerSave;
        this.totalSaved = totalSaved;
        this.weeklyHistory = weeklyHistory;
        this.subscribedAt = subscribedAt;
    }

    public static PassSubscriptionResponse from(
            final PassSubscription subscription, final int totalSaved, final List<Boolean> weeklyHistory) {
        if (subscription == null) {
            throw new IllegalArgumentException("구독 정보는 null일 수 없습니다.");
        }
        return new PassSubscriptionResponse(
                subscription.getId(),
                subscription.getPassProduct().getId(),
                subscription.getPassName(),
                subscription.getSavingAmount(),
                totalSaved,
                weeklyHistory,
                subscription.getSubscribedAt());
    }

    /** 구독 신청 응답용 (totalSaved=0, weeklyHistory 없음) */
    public static PassSubscriptionResponse fromSubscribe(final PassSubscription subscription) {
        return new PassSubscriptionResponse(
                subscription.getId(),
                subscription.getPassProduct().getId(),
                subscription.getPassName(),
                subscription.getSavingAmount(),
                0,
                List.of(false, false, false, false, false, false, false),
                subscription.getSubscribedAt());
    }

    public Long getSubscriptionId() { return subscriptionId; }
    public Long getPassId() { return passId; }
    public String getName() { return name; }
    public Integer getAmountPerSave() { return amountPerSave; }
    public Integer getTotalSaved() { return totalSaved; }
    public List<Boolean> getWeeklyHistory() { return weeklyHistory; }
    public LocalDateTime getSubscribedAt() { return subscribedAt; }
}
