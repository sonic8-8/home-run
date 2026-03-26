package io.ssafy.p.j14c103.homerun.domain.seedmoney;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "시드머니계좌")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeedmoneyAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "시드머니계좌번호")
    private Long id;

    @Column(name = "유저번호", nullable = false, unique = true)
    private Long userId;

    @Column(name = "은행명")
    private String bankName;

    @Column(name = "마스킹계좌번호")
    private String accountNumber;

    @Column(name = "잔액스냅샷")
    private Integer balanceSnapshot;

    @Column(name = "수정일시")
    private LocalDateTime updatedAt;

    private SeedmoneyAccount(final Long userId, final String bankName, final String accountNumber) {
        this.userId = userId;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.balanceSnapshot = 0;
        this.updatedAt = LocalDateTime.now();
    }

    public static SeedmoneyAccount create(final Long userId, final String bankName, final String accountNumber) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException("계좌번호는 필수입니다.");
        }
        return new SeedmoneyAccount(userId, bankName, accountNumber);
    }

    public void updateBalance(final Integer newBalance) {
        if (newBalance == null) {
            throw new IllegalArgumentException("잔액은 null일 수 없습니다.");
        }
        this.balanceSnapshot = newBalance;
        this.updatedAt = LocalDateTime.now();
    }

    public void syncSnapshot(
            final String bankName,
            final String accountNumber,
            final Integer balanceSnapshot
    ) {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException("계좌번호는 필수입니다.");
        }
        if (balanceSnapshot == null) {
            throw new IllegalArgumentException("잔액은 null일 수 없습니다.");
        }

        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.balanceSnapshot = balanceSnapshot;
        this.updatedAt = LocalDateTime.now();
    }
}
