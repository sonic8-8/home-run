package io.ssafy.p.j14c103.homerun.domain.gamesession.loan;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 대출 심사/신청 워크플로우.
 * PENDING → APPROVED / REJECTED → CONFIRMED
 */
@Getter
@Entity
@Table(name = "loan_applications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "loan_application_id")
    private Integer loanApplicationId;

    @Column(name = "game_session_id", nullable = false)
    private Integer gameSessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "loan_type", length = 20)
    private LoanType loanType;

    @Column(name = "product_id", length = 100)
    private String productId;

    @Column(name = "property_id")
    private Integer propertyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "application_status", length = 20)
    private LoanApplicationStatus applicationStatus;

    @Column(name = "approved_limit_amount")
    private Integer approvedLimitAmount;

    @Column(name = "rejection_reason", columnDefinition = "text")
    private String rejectionReason;

    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    private LoanApplication(
            final Integer gameSessionId,
            final LoanType loanType,
            final String productId,
            final Integer propertyId
    ) {
        this.gameSessionId = gameSessionId;
        this.loanType = loanType;
        this.productId = productId;
        this.propertyId = propertyId;
        this.applicationStatus = LoanApplicationStatus.PENDING;
        this.appliedAt = LocalDateTime.now();
    }

    public static LoanApplication create(
            final Integer gameSessionId,
            final LoanType loanType,
            final String productId,
            final Integer propertyId
    ) {
        return new LoanApplication(gameSessionId, loanType, productId, propertyId);
    }

    public void approve(final Integer approvedLimit) {
        this.applicationStatus = LoanApplicationStatus.APPROVED;
        this.approvedLimitAmount = approvedLimit;
    }

    public void reject(final String reason) {
        this.applicationStatus = LoanApplicationStatus.REJECTED;
        this.rejectionReason = reason;
    }

    public void confirm() {
        this.applicationStatus = LoanApplicationStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    public boolean isApproved() {
        return this.applicationStatus == LoanApplicationStatus.APPROVED;
    }
}
