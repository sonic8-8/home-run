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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "owned_cards")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OwnedCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "owned_card_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_product_id", nullable = false)
    private CardProduct cardProduct;

    @Column(name = "card_alias", length = 100)
    private String cardAlias;

    @Column(name = "masked_card_no", nullable = false, length = 30)
    private String maskedCardNo;

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;

    @Column(name = "active_yn", nullable = false)
    private Boolean activeYn;

    private OwnedCard(
            final Long userId,
            final CardProduct cardProduct,
            final String cardAlias,
            final String maskedCardNo,
            final LocalDateTime openedAt
    ) {
        this.userId = userId;
        this.cardProduct = cardProduct;
        this.cardAlias = cardAlias;
        this.maskedCardNo = maskedCardNo;
        this.openedAt = openedAt;
        this.activeYn = true;
    }

    public static OwnedCard create(
            final Long userId,
            final CardProduct cardProduct,
            final String cardAlias,
            final String maskedCardNo,
            final LocalDateTime openedAt
    ) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (cardProduct == null) {
            throw new IllegalArgumentException("카드 상품은 필수입니다.");
        }
        if (maskedCardNo == null || maskedCardNo.isBlank()) {
            throw new IllegalArgumentException("마스킹 카드번호는 필수입니다.");
        }
        if (openedAt == null) {
            throw new IllegalArgumentException("개설일은 필수입니다.");
        }

        return new OwnedCard(userId, cardProduct, cardAlias, maskedCardNo, openedAt);
    }
}
