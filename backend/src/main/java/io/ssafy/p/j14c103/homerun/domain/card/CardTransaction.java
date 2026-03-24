package io.ssafy.p.j14c103.homerun.domain.card;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "card_transactions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CardTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_transaction_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owned_card_id", nullable = false)
    private OwnedCard ownedCard;

    @Column(name = "category_id", nullable = false, length = 50)
    private String categoryId;

    @Column(name = "category_name", nullable = false, length = 50)
    private String categoryName;

    @Column(name = "merchant_name", nullable = false, length = 100)
    private String merchantName;

    @Column(name = "payment_amount", nullable = false)
    private Integer paymentAmount;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private CardTransaction(
            final Long userId,
            final OwnedCard ownedCard,
            final String categoryId,
            final String categoryName,
            final String merchantName,
            final Integer paymentAmount,
            final LocalDate paymentDate
    ) {
        this.userId = userId;
        this.ownedCard = ownedCard;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.merchantName = merchantName;
        this.paymentAmount = paymentAmount;
        this.paymentDate = paymentDate;
        this.createdAt = LocalDateTime.now();
    }

    public static CardTransaction create(
            final Long userId,
            final OwnedCard ownedCard,
            final String categoryId,
            final String categoryName,
            final String merchantName,
            final Integer paymentAmount,
            final LocalDate paymentDate
    ) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (ownedCard == null) {
            throw new IllegalArgumentException("보유 카드는 필수입니다.");
        }
        if (categoryId == null || categoryId.isBlank()) {
            throw new IllegalArgumentException("카테고리 ID는 필수입니다.");
        }
        if (categoryName == null || categoryName.isBlank()) {
            throw new IllegalArgumentException("카테고리명은 필수입니다.");
        }
        if (merchantName == null || merchantName.isBlank()) {
            throw new IllegalArgumentException("가맹점명은 필수입니다.");
        }
        if (paymentAmount == null || paymentAmount <= 0) {
            throw new IllegalArgumentException("결제금액은 0보다 커야 합니다.");
        }
        if (paymentDate == null) {
            throw new IllegalArgumentException("결제일은 필수입니다.");
        }

        return new CardTransaction(
                userId,
                ownedCard,
                categoryId,
                categoryName,
                merchantName,
                paymentAmount,
                paymentDate
        );
    }
}
