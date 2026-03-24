package io.ssafy.p.j14c103.homerun.domain.financial;

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

@Getter
@Entity
@Table(name = "user_financial_transactions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserFinancialTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_financial_transaction_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_financial_product_id")
    private Long userFinancialProductId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    private FinancialTransactionType transactionType;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private UserFinancialTransaction(
            final Long userId,
            final Long userFinancialProductId,
            final FinancialTransactionType transactionType,
            final Integer amount,
            final LocalDateTime occurredAt
    ) {
        this.userId = userId;
        this.userFinancialProductId = userFinancialProductId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.occurredAt = occurredAt;
        this.createdAt = LocalDateTime.now();
    }

    public static UserFinancialTransaction create(
            final Long userId,
            final Long userFinancialProductId,
            final FinancialTransactionType transactionType,
            final Integer amount,
            final LocalDateTime occurredAt
    ) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (transactionType == null) {
            throw new IllegalArgumentException("금융거래 유형은 필수입니다.");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("금액은 0보다 커야 합니다.");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("거래일시는 필수입니다.");
        }

        return new UserFinancialTransaction(userId, userFinancialProductId, transactionType, amount, occurredAt);
    }
}
