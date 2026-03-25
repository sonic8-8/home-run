package io.ssafy.p.j14c103.homerun.domain.financial;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "user_financial_summaries")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserFinancialSummary {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "total_asset_amount", nullable = false)
    private Integer totalAssetAmount;

    @Column(name = "total_debt_amount", nullable = false)
    private Integer totalDebtAmount;

    @Column(name = "net_asset_amount", nullable = false)
    private Integer netAssetAmount;

    @Column(name = "cash_asset_amount", nullable = false)
    private Integer cashAssetAmount;

    @Column(name = "saving_asset_amount", nullable = false)
    private Integer savingAssetAmount;

    @Column(name = "investment_asset_amount", nullable = false)
    private Integer investmentAssetAmount;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private UserFinancialSummary(final Long userId) {
        this.userId = userId;
        this.totalAssetAmount = 0;
        this.totalDebtAmount = 0;
        this.netAssetAmount = 0;
        this.cashAssetAmount = 0;
        this.savingAssetAmount = 0;
        this.investmentAssetAmount = 0;
        this.updatedAt = LocalDateTime.now();
    }

    public static UserFinancialSummary create(final Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        return new UserFinancialSummary(userId);
    }

    public void refresh(
            final int totalAssetAmount,
            final int totalDebtAmount,
            final int netAssetAmount,
            final int cashAssetAmount,
            final int savingAssetAmount,
            final int investmentAssetAmount
    ) {
        this.totalAssetAmount = totalAssetAmount;
        this.totalDebtAmount = totalDebtAmount;
        this.netAssetAmount = netAssetAmount;
        this.cashAssetAmount = cashAssetAmount;
        this.savingAssetAmount = savingAssetAmount;
        this.investmentAssetAmount = investmentAssetAmount;
        this.updatedAt = LocalDateTime.now();
    }
}
