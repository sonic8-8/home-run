package io.ssafy.p.j14c103.homerun.domain.seedmoney;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "seedmoney_accounts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeedmoneyAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private String accountNumber;

    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal balanceSnapshot;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private SeedmoneyAccount(final Long userId, final String accountNumber) {
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.balanceSnapshot = BigDecimal.ZERO;
        this.updatedAt = LocalDateTime.now();
    }

    public static SeedmoneyAccount create(final Long userId, final String accountNumber) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException("계좌번호는 필수입니다.");
        }
        return new SeedmoneyAccount(userId, accountNumber);
    }

    public void updateBalance(final BigDecimal newBalance) {
        if (newBalance == null) {
            throw new IllegalArgumentException("잔액은 null일 수 없습니다.");
        }
        this.balanceSnapshot = newBalance;
        this.updatedAt = LocalDateTime.now();
    }
}
