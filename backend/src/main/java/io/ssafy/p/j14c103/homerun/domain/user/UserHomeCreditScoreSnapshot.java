package io.ssafy.p.j14c103.homerun.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "user_home_credit_score_snapshots",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_home_credit_score_snapshots__user_id__score_month_start",
                columnNames = {"user_id", "score_month_start"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserHomeCreditScoreSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_home_credit_score_snapshot_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "score_month_start", nullable = false)
    private LocalDate scoreMonthStart;

    @Enumerated(EnumType.STRING)
    @Column(name = "snapshot_type", nullable = false, length = 20)
    private HomeCreditScoreSnapshotType snapshotType;

    @Column(name = "payment_history", nullable = false)
    private Integer paymentHistory;

    @Column(name = "amounts_owed", nullable = false)
    private Integer amountsOwed;

    @Column(name = "credit_length", nullable = false)
    private Integer creditLength;

    @Column(name = "credit_mix", nullable = false)
    private Integer creditMix;

    @Column(name = "new_credit", nullable = false)
    private Integer newCredit;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private UserHomeCreditScoreSnapshot(
            final Long userId,
            final LocalDate scoreMonthStart,
            final HomeCreditScoreSnapshotType snapshotType,
            final Integer paymentHistory,
            final Integer amountsOwed,
            final Integer creditLength,
            final Integer creditMix,
            final Integer newCredit,
            final LocalDateTime createdAt
    ) {
        validate(userId, scoreMonthStart, snapshotType, paymentHistory, amountsOwed, creditLength, creditMix, newCredit, createdAt);
        this.userId = userId;
        this.scoreMonthStart = scoreMonthStart;
        this.snapshotType = snapshotType;
        this.paymentHistory = paymentHistory;
        this.amountsOwed = amountsOwed;
        this.creditLength = creditLength;
        this.creditMix = creditMix;
        this.newCredit = newCredit;
        this.createdAt = createdAt;
    }

    public static UserHomeCreditScoreSnapshot create(
            final Long userId,
            final LocalDate scoreMonthStart,
            final HomeCreditScoreSnapshotType snapshotType,
            final Integer paymentHistory,
            final Integer amountsOwed,
            final Integer creditLength,
            final Integer creditMix,
            final Integer newCredit,
            final LocalDateTime createdAt
    ) {
        return new UserHomeCreditScoreSnapshot(
                userId,
                scoreMonthStart,
                snapshotType,
                paymentHistory,
                amountsOwed,
                creditLength,
                creditMix,
                newCredit,
                createdAt
        );
    }

    public void update(
            final HomeCreditScoreSnapshotType snapshotType,
            final Integer paymentHistory,
            final Integer amountsOwed,
            final Integer creditLength,
            final Integer creditMix,
            final Integer newCredit,
            final LocalDateTime createdAt
    ) {
        validate(userId, scoreMonthStart, snapshotType, paymentHistory, amountsOwed, creditLength, creditMix, newCredit, createdAt);
        this.snapshotType = snapshotType;
        this.paymentHistory = paymentHistory;
        this.amountsOwed = amountsOwed;
        this.creditLength = creditLength;
        this.creditMix = creditMix;
        this.newCredit = newCredit;
        this.createdAt = createdAt;
    }

    private void validate(
            final Long userId,
            final LocalDate scoreMonthStart,
            final HomeCreditScoreSnapshotType snapshotType,
            final Integer paymentHistory,
            final Integer amountsOwed,
            final Integer creditLength,
            final Integer creditMix,
            final Integer newCredit,
            final LocalDateTime createdAt
    ) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (scoreMonthStart == null) {
            throw new IllegalArgumentException("점수 기준 월은 필수입니다.");
        }
        if (snapshotType == null) {
            throw new IllegalArgumentException("스냅샷 유형은 필수입니다.");
        }
        if (paymentHistory == null || amountsOwed == null || creditLength == null || creditMix == null || newCredit == null) {
            throw new IllegalArgumentException("개별 CSS 항목 점수는 필수입니다.");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("생성 시각은 필수입니다.");
        }
    }
}
