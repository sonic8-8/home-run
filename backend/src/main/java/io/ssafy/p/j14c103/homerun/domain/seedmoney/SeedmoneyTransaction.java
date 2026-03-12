package io.ssafy.p.j14c103.homerun.domain.seedmoney;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "seedmoney_transactions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeedmoneyTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    private Long subscriptionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal amount;

    private String counterpartyAccountMasked;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private SeedmoneyTransaction(
            final Long userId,
            final Long subscriptionId,
            final TransactionType transactionType,
            final BigDecimal amount,
            final String counterpartyAccountMasked) {
        this.userId = userId;
        this.subscriptionId = subscriptionId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.counterpartyAccountMasked = counterpartyAccountMasked;
        this.createdAt = LocalDateTime.now();
    }

    public static SeedmoneyTransaction createSave(
            final Long userId,
            final Long subscriptionId,
            final BigDecimal amount) {
        validateCommonFields(userId, amount);
        return new SeedmoneyTransaction(userId, subscriptionId, TransactionType.SAVE, amount, null);
    }

    public static SeedmoneyTransaction createDeposit(
            final Long userId,
            final BigDecimal amount,
            final String fromAccountMasked) {
        validateCommonFields(userId, amount);
        return new SeedmoneyTransaction(userId, null, TransactionType.DEPOSIT, amount, fromAccountMasked);
    }

    public static SeedmoneyTransaction createTransfer(
            final Long userId,
            final BigDecimal amount,
            final String toAccountMasked) {
        validateCommonFields(userId, amount);
        return new SeedmoneyTransaction(userId, null, TransactionType.TRANSFER, amount, toAccountMasked);
    }

    private static void validateCommonFields(final Long userId, final BigDecimal amount) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("금액은 0보다 커야 합니다.");
        }
    }
}
