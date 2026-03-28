package io.ssafy.p.j14c103.homerun.domain.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigInteger;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "user_accounts",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_accounts__user_id__account_type",
                columnNames = {"user_id", "account_type"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_account_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    @Column(name = "bank_code", nullable = false, length = 3)
    private String bankCode;

    @Column(name = "bank_name", nullable = false, length = 100)
    private String bankName;

    @Column(name = "account_number", nullable = false, length = 50)
    private String accountNumber;

    @Column(name = "balance_snapshot_amount", nullable = false)
    private Long balanceSnapshot;

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;

    @Column(name = "active_yn", nullable = false)
    private Boolean activeYn;

    @Column(name = "ssafy_sync_initialized", nullable = false)
    private Boolean ssafySyncInitialized;

    @Column(name = "last_synced_ssafy_transaction_unique_no", length = 50)
    private String lastSyncedSsafyTransactionUniqueNo;

    @Column(name = "main_initial_history_seeded", nullable = false)
    private Boolean mainInitialHistorySeeded;

    private UserAccount(
            final Long userId,
            final AccountType accountType,
            final String bankCode,
            final String bankName,
            final String accountNumber,
            final Long balanceSnapshot) {
        this.userId = userId;
        this.accountType = accountType;
        this.bankCode = bankCode;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.balanceSnapshot = balanceSnapshot;
        this.openedAt = LocalDateTime.now();
        this.activeYn = true;
        this.ssafySyncInitialized = false;
        this.lastSyncedSsafyTransactionUniqueNo = null;
        this.mainInitialHistorySeeded = false;
    }

    public static UserAccount create(
            final Long userId,
            final AccountType accountType,
            final String bankCode,
            final String bankName,
            final String accountNumber,
            final int balanceSnapshot) {
        return create(userId, accountType, bankCode, bankName, accountNumber, Long.valueOf(balanceSnapshot));
    }

    public static UserAccount create(
            final Long userId,
            final AccountType accountType,
            final String bankCode,
            final String bankName,
            final String accountNumber,
            final Long balanceSnapshot) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (accountType == null) {
            throw new IllegalArgumentException("계좌 유형은 필수입니다.");
        }
        if (bankCode == null || bankCode.isBlank()) {
            throw new IllegalArgumentException("은행 코드는 필수입니다.");
        }
        if (bankName == null || bankName.isBlank()) {
            throw new IllegalArgumentException("은행명은 필수입니다.");
        }
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException("계좌번호는 필수입니다.");
        }
        if (balanceSnapshot == null || balanceSnapshot < 0) {
            throw new IllegalArgumentException("잔액은 0 이상이어야 합니다.");
        }

        return new UserAccount(userId, accountType, bankCode, bankName, accountNumber, balanceSnapshot);
    }

    public void updateBalance(final long newBalance) {
        if (newBalance < 0) {
            throw new IllegalArgumentException("잔액은 0 이상이어야 합니다.");
        }
        this.balanceSnapshot = newBalance;
    }

    public void initializeSsafySync(final String baselineTransactionUniqueNo) {
        this.ssafySyncInitialized = true;
        this.lastSyncedSsafyTransactionUniqueNo = normalizeTransactionUniqueNo(baselineTransactionUniqueNo);
    }

    public void advanceSsafySync(final String latestTransactionUniqueNo) {
        this.ssafySyncInitialized = true;

        final String normalized = normalizeTransactionUniqueNo(latestTransactionUniqueNo);
        if (normalized == null) {
            return;
        }
        if (lastSyncedSsafyTransactionUniqueNo == null || isGreaterTransactionUniqueNo(normalized, lastSyncedSsafyTransactionUniqueNo)) {
            this.lastSyncedSsafyTransactionUniqueNo = normalized;
        }
    }

    public void markMainInitialHistorySeeded() {
        if (accountType != AccountType.MAIN) {
            throw new IllegalStateException("주계좌만 초기 원장 시드 상태를 가질 수 있습니다.");
        }
        this.mainInitialHistorySeeded = true;
    }

    private String normalizeTransactionUniqueNo(final String transactionUniqueNo) {
        if (transactionUniqueNo == null || transactionUniqueNo.isBlank()) {
            return null;
        }
        return transactionUniqueNo.trim();
    }

    private boolean isGreaterTransactionUniqueNo(
            final String candidate,
            final String current
    ) {
        try {
            return new BigInteger(candidate).compareTo(new BigInteger(current)) > 0;
        } catch (final NumberFormatException exception) {
            return candidate.compareTo(current) > 0;
        }
    }
}
