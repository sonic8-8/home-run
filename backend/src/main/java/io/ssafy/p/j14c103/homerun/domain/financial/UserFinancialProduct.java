package io.ssafy.p.j14c103.homerun.domain.financial;

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
@Table(name = "user_financial_products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserFinancialProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_financial_product_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 30)
    private FinancialProductType productType;

    @Column(name = "institution_name", nullable = false, length = 100)
    private String institutionName;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "current_balance_amount", nullable = false)
    private Integer currentBalanceAmount;

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;

    @Column(name = "active_yn", nullable = false)
    private Boolean activeYn;

    private UserFinancialProduct(
            final Long userId,
            final FinancialProductType productType,
            final String institutionName,
            final String productName,
            final Integer currentBalanceAmount,
            final LocalDateTime openedAt
    ) {
        this.userId = userId;
        this.productType = productType;
        this.institutionName = institutionName;
        this.productName = productName;
        this.currentBalanceAmount = currentBalanceAmount;
        this.openedAt = openedAt;
        this.activeYn = true;
    }

    public static UserFinancialProduct create(
            final Long userId,
            final FinancialProductType productType,
            final String institutionName,
            final String productName,
            final Integer currentBalanceAmount,
            final LocalDateTime openedAt
    ) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (productType == null) {
            throw new IllegalArgumentException("금융상품 유형은 필수입니다.");
        }
        if (institutionName == null || institutionName.isBlank()) {
            throw new IllegalArgumentException("기관명은 필수입니다.");
        }
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("상품명은 필수입니다.");
        }
        if (currentBalanceAmount == null || currentBalanceAmount < 0) {
            throw new IllegalArgumentException("잔액은 0 이상이어야 합니다.");
        }
        if (openedAt == null) {
            throw new IllegalArgumentException("개설일은 필수입니다.");
        }

        return new UserFinancialProduct(
                userId,
                productType,
                institutionName,
                productName,
                currentBalanceAmount,
                openedAt
        );
    }

    public void updateBalance(final Integer currentBalanceAmount) {
        if (currentBalanceAmount == null || currentBalanceAmount < 0) {
            throw new IllegalArgumentException("잔액은 0 이상이어야 합니다.");
        }
        this.currentBalanceAmount = currentBalanceAmount;
    }
}
