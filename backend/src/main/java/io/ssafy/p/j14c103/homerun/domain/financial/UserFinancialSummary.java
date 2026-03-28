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
    private Long totalAssetAmount;

    @Column(name = "total_debt_amount", nullable = false)
    private Long totalDebtAmount;

    @Column(name = "net_asset_amount", nullable = false)
    private Long netAssetAmount;

    @Column(name = "cash_asset_amount", nullable = false)
    private Long cashAssetAmount;

    @Column(name = "saving_asset_amount", nullable = false)
    private Long savingAssetAmount;

    @Column(name = "investment_asset_amount", nullable = false)
    private Long investmentAssetAmount;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private UserFinancialSummary(final Long userId) {
        this.userId = userId;
        this.totalAssetAmount = 0L;
        this.totalDebtAmount = 0L;
        this.netAssetAmount = 0L;
        this.cashAssetAmount = 0L;
        this.savingAssetAmount = 0L;
        this.investmentAssetAmount = 0L;
        this.updatedAt = LocalDateTime.now();
    }

    public static UserFinancialSummary create(final Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        return new UserFinancialSummary(userId);
    }

    public void refresh(
            final long totalAssetAmount,
            final long totalDebtAmount,
            final long netAssetAmount,
            final long cashAssetAmount,
            final long savingAssetAmount,
            final long investmentAssetAmount
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
