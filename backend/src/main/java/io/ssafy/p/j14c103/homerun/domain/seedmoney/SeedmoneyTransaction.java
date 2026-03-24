package io.ssafy.p.j14c103.homerun.domain.seedmoney;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "시드머니거래내역")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeedmoneyTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "거래번호")
    private Long id;

    @Column(name = "유저번호", nullable = false)
    private Long userId;

    @Column(name = "패스번호")
    private Long passId;

    @Column(name = "거래유형")
    private String transactionType;

    @Column(name = "거래금액")
    private Integer amount;

    @Column(name = "상대계좌마스킹")
    private String counterpartyAccountMasked;

    @Column(name = "생성일시")
    private LocalDateTime createdAt;

    private SeedmoneyTransaction(
            final Long userId,
            final Long passId,
            final String transactionType,
            final Integer amount,
            final String counterpartyAccountMasked) {
        this.userId = userId;
        this.passId = passId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.counterpartyAccountMasked = counterpartyAccountMasked;
        this.createdAt = LocalDateTime.now();
    }

    public static SeedmoneyTransaction createSave(
            final Long userId,
            final Long passId,
            final Integer amount) {
        validateCommonFields(userId, amount);
        return new SeedmoneyTransaction(userId, passId, "SAVE", amount, null);
    }

    public static SeedmoneyTransaction createDeposit(
            final Long userId,
            final Integer amount,
            final String fromAccountMasked) {
        validateCommonFields(userId, amount);
        return new SeedmoneyTransaction(userId, null, "DEPOSIT", amount, fromAccountMasked);
    }

    public static SeedmoneyTransaction createTransfer(
            final Long userId,
            final Integer amount,
            final String toAccountMasked) {
        validateCommonFields(userId, amount);
        return new SeedmoneyTransaction(userId, null, "TRANSFER", amount, toAccountMasked);
    }

    private static void validateCommonFields(final Long userId, final Integer amount) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("금액은 0보다 커야 합니다.");
        }
    }
}
