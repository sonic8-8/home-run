package io.ssafy.p.j14c103.homerun.domain.card;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "card_products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CardProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_product_id")
    private Long id;

    @Column(name = "card_name", nullable = false, length = 100)
    private String cardName;

    @Column(name = "card_issuer_name", length = 100)
    private String cardIssuerName;

    @Column(name = "card_description", columnDefinition = "TEXT")
    private String cardDescription;

    @Column(name = "baseline_performance_amount")
    private Integer baselinePerformanceAmount;

    @Column(name = "max_benefit_limit_amount")
    private Integer maxBenefitLimitAmount;

    @Column(name = "active_benefits", columnDefinition = "TEXT")
    private String activeBenefits;

    @Column(name = "card_image_url", length = 255)
    private String cardImageUrl;

    @Column(name = "active_yn", nullable = false)
    private boolean activeYn;

    private CardProduct(
            final String cardName,
            final String cardIssuerName,
            final String cardDescription,
            final Integer baselinePerformanceAmount,
            final Integer maxBenefitLimitAmount,
            final String activeBenefits,
            final String cardImageUrl,
            final boolean activeYn
    ) {
        this.cardName = cardName;
        this.cardIssuerName = cardIssuerName;
        this.cardDescription = cardDescription;
        this.baselinePerformanceAmount = baselinePerformanceAmount;
        this.maxBenefitLimitAmount = maxBenefitLimitAmount;
        this.activeBenefits = activeBenefits;
        this.cardImageUrl = cardImageUrl;
        this.activeYn = activeYn;
    }

    public static CardProduct create(
            final String cardName,
            final String cardIssuerName,
            final String cardDescription,
            final Integer baselinePerformanceAmount,
            final Integer maxBenefitLimitAmount,
            final String activeBenefits,
            final String cardImageUrl,
            final boolean activeYn
    ) {
        if (cardName == null || cardName.isBlank()) {
            throw new IllegalArgumentException("카드명은 필수입니다.");
        }

        return new CardProduct(
                cardName,
                cardIssuerName,
                cardDescription,
                baselinePerformanceAmount,
                maxBenefitLimitAmount,
                activeBenefits,
                cardImageUrl,
                activeYn
        );
    }
}
