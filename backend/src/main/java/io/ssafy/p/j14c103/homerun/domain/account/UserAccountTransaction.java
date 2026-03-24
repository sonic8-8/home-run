package io.ssafy.p.j14c103.homerun.domain.account;

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
@Table(name = "user_account_transactions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAccountTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_account_transaction_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    @Column(name = "pass_subscription_id")
    private Long passSubscriptionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    private AccountTransactionType transactionType;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "counterparty_account_number", length = 50)
    private String counterpartyAccountNumber;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private UserAccountTransaction(
            final Long userId,
            final AccountType accountType,
            final Long passSubscriptionId,
            final AccountTransactionType transactionType,
            final Integer amount,
            final String counterpartyAccountNumber,
            final LocalDateTime createdAt) {
        this.userId = userId;
        this.accountType = accountType;
        this.passSubscriptionId = passSubscriptionId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.counterpartyAccountNumber = counterpartyAccountNumber;
        this.createdAt = createdAt;
    }

    public static UserAccountTransaction create(
            final Long userId,
            final AccountType accountType,
            final Long passSubscriptionId,
            final AccountTransactionType transactionType,
            final Integer amount,
            final String counterpartyAccountNumber) {
        return create(
                userId,
                accountType,
                passSubscriptionId,
                transactionType,
                amount,
                counterpartyAccountNumber,
                LocalDateTime.now()
        );
    }

    public static UserAccountTransaction create(
            final Long userId,
            final AccountType accountType,
            final Long passSubscriptionId,
            final AccountTransactionType transactionType,
            final Integer amount,
            final String counterpartyAccountNumber,
            final LocalDateTime createdAt) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (accountType == null) {
            throw new IllegalArgumentException("계좌 유형은 필수입니다.");
        }
        if (transactionType == null) {
            throw new IllegalArgumentException("거래 유형은 필수입니다.");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("거래 금액은 0보다 커야 합니다.");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("거래일시는 필수입니다.");
        }

        return new UserAccountTransaction(
                userId,
                accountType,
                passSubscriptionId,
                transactionType,
                amount,
                counterpartyAccountNumber,
                createdAt);
    }
}
