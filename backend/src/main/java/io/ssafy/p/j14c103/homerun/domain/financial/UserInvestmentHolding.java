package io.ssafy.p.j14c103.homerun.domain.financial;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "user_investment_holdings")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserInvestmentHolding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_investment_holding_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_financial_product_id", nullable = false)
    private Long userFinancialProductId;

    @Column(name = "stock_code", nullable = false, length = 20)
    private String stockCode;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "average_purchase_price_amount", nullable = false)
    private Integer averagePurchasePriceAmount;

    @Column(name = "current_price_amount", nullable = false)
    private Integer currentPriceAmount;

    @Column(name = "price_updated_at", nullable = false)
    private LocalDateTime priceUpdatedAt;

    @Column(name = "active_yn", nullable = false)
    private Boolean activeYn;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private UserInvestmentHolding(
            final Long userId,
            final Long userFinancialProductId,
            final String stockCode,
            final Integer quantity,
            final Integer averagePurchasePriceAmount,
            final Integer currentPriceAmount,
            final LocalDateTime priceUpdatedAt
    ) {
        this.userId = userId;
        this.userFinancialProductId = userFinancialProductId;
        this.stockCode = stockCode;
        this.quantity = quantity;
        this.averagePurchasePriceAmount = averagePurchasePriceAmount;
        this.currentPriceAmount = currentPriceAmount;
        this.priceUpdatedAt = priceUpdatedAt;
        this.activeYn = true;
        this.createdAt = LocalDateTime.now();
    }

    public static UserInvestmentHolding create(
            final Long userId,
            final Long userFinancialProductId,
            final String stockCode,
            final Integer quantity,
            final Integer averagePurchasePriceAmount,
            final Integer currentPriceAmount
    ) {
        return create(
                userId,
                userFinancialProductId,
                stockCode,
                quantity,
                averagePurchasePriceAmount,
                currentPriceAmount,
                LocalDateTime.now()
        );
    }

    public static UserInvestmentHolding create(
            final Long userId,
            final Long userFinancialProductId,
            final String stockCode,
            final Integer quantity,
            final Integer averagePurchasePriceAmount,
            final Integer currentPriceAmount,
            final LocalDateTime priceUpdatedAt
    ) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (userFinancialProductId == null) {
            throw new IllegalArgumentException("금융상품 ID는 필수입니다.");
        }
        if (stockCode == null || stockCode.isBlank()) {
            throw new IllegalArgumentException("종목코드는 필수입니다.");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("보유수량은 1 이상이어야 합니다.");
        }
        if (averagePurchasePriceAmount == null || averagePurchasePriceAmount <= 0) {
            throw new IllegalArgumentException("평단은 0보다 커야 합니다.");
        }
        if (currentPriceAmount == null || currentPriceAmount <= 0) {
            throw new IllegalArgumentException("현재가는 0보다 커야 합니다.");
        }
        if (priceUpdatedAt == null) {
            throw new IllegalArgumentException("가격 갱신 시각은 필수입니다.");
        }

        return new UserInvestmentHolding(
                userId,
                userFinancialProductId,
                stockCode,
                quantity,
                averagePurchasePriceAmount,
                currentPriceAmount,
                priceUpdatedAt
        );
    }

    public int getCurrentValueAmount() {
        return currentPriceAmount * quantity;
    }

    public int getPurchaseAmount() {
        return averagePurchasePriceAmount * quantity;
    }

    public void updateCurrentPrice(final Integer currentPriceAmount, final LocalDateTime priceUpdatedAt) {
        if (currentPriceAmount == null || currentPriceAmount <= 0) {
            throw new IllegalArgumentException("현재가는 0보다 커야 합니다.");
        }
        if (priceUpdatedAt == null) {
            throw new IllegalArgumentException("가격 갱신 시각은 필수입니다.");
        }
        this.currentPriceAmount = currentPriceAmount;
        this.priceUpdatedAt = priceUpdatedAt;
    }

    public void touchPriceUpdatedAt(final LocalDateTime priceUpdatedAt) {
        if (priceUpdatedAt == null) {
            throw new IllegalArgumentException("가격 갱신 시각은 필수입니다.");
        }
        this.priceUpdatedAt = priceUpdatedAt;
    }
}
