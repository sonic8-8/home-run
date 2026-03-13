package io.ssafy.p.j14c103.homerun.api.service.pass.response;

import io.ssafy.p.j14c103.homerun.domain.pass.PassSubscription;

import java.time.LocalDateTime;

public class PassSubscriptionResponse {

    private final Long id;
    private final String passName;
    private final Integer savingAmount;
    private final String sourceAccountNo;
    private final boolean active;
    private final LocalDateTime subscribedAt;

    private PassSubscriptionResponse(
            final Long id, final String passName, final Integer savingAmount,
            final String sourceAccountNo, final boolean active, final LocalDateTime subscribedAt) {
        this.id = id;
        this.passName = passName;
        this.savingAmount = savingAmount;
        this.sourceAccountNo = sourceAccountNo;
        this.active = active;
        this.subscribedAt = subscribedAt;
    }

    public static PassSubscriptionResponse from(final PassSubscription subscription) {
        if (subscription == null) {
            throw new IllegalArgumentException("구독 정보는 null일 수 없습니다.");
        }
        return new PassSubscriptionResponse(
                subscription.getId(),
                subscription.getPassName(),
                subscription.getSavingAmount(),
                subscription.getSourceAccountNo(),
                subscription.getIsActive(),
                subscription.getSubscribedAt());
    }

    public Long getId() { return id; }
    public String getPassName() { return passName; }
    public Integer getSavingAmount() { return savingAmount; }
    public String getSourceAccountNo() { return sourceAccountNo; }
    public boolean isActive() { return active; }
    public LocalDateTime getSubscribedAt() { return subscribedAt; }
}
