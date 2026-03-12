package io.ssafy.p.j14c103.homerun.api.service.pass.response;

import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyTransaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PassHistoryResponse {

    private final Long id;
    private final String transactionType;
    private final BigDecimal amount;
    private final String counterpartyAccountMasked;
    private final LocalDateTime createdAt;

    private PassHistoryResponse(
            final Long id,
            final String transactionType,
            final BigDecimal amount,
            final String counterpartyAccountMasked,
            final LocalDateTime createdAt) {
        this.id = id;
        this.transactionType = transactionType;
        this.amount = amount;
        this.counterpartyAccountMasked = counterpartyAccountMasked;
        this.createdAt = createdAt;
    }

    public static PassHistoryResponse from(final SeedmoneyTransaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("거래 내역은 null일 수 없습니다.");
        }
        return new PassHistoryResponse(
                transaction.getId(),
                transaction.getTransactionType().getDisplayName(),
                transaction.getAmount(),
                transaction.getCounterpartyAccountMasked(),
                transaction.getCreatedAt());
    }

    public Long getId() {
        return id;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCounterpartyAccountMasked() {
        return counterpartyAccountMasked;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
