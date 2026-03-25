package io.ssafy.p.j14c103.homerun.domain.financial;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "financial_product_templates")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FinancialProductTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "financial_product_template_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 30)
    private FinancialProductType productType;

    @Column(name = "institution_name", nullable = false, length = 100)
    private String institutionName;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "active_yn", nullable = false)
    private Boolean activeYn;

    private FinancialProductTemplate(
            final FinancialProductType productType,
            final String institutionName,
            final String productName
    ) {
        this.productType = productType;
        this.institutionName = institutionName;
        this.productName = productName;
        this.activeYn = true;
    }

    public static FinancialProductTemplate create(
            final FinancialProductType productType,
            final String institutionName,
            final String productName
    ) {
        if (productType == null) {
            throw new IllegalArgumentException("금융상품 유형은 필수입니다.");
        }
        if (institutionName == null || institutionName.isBlank()) {
            throw new IllegalArgumentException("기관명은 필수입니다.");
        }
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("상품명은 필수입니다.");
        }

        return new FinancialProductTemplate(productType, institutionName, productName);
    }
}
