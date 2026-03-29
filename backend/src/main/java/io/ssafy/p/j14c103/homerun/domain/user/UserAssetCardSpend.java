package io.ssafy.p.j14c103.homerun.domain.user;

import io.ssafy.p.j14c103.homerun.domain.spending.SpendingCategory;
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
@Table(name = "user_asset_card_spends")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAssetCardSpend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_asset_card_spend_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private SpendingCategory category;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private UserAssetCardSpend(final Long userId, final SpendingCategory category, final Long amount) {
        validate(userId, category, amount);
        this.userId = userId;
        this.category = category;
        this.amount = amount;
        this.createdAt = LocalDateTime.now();
    }

    public static UserAssetCardSpend create(final Long userId, final SpendingCategory category, final Long amount) {
        return new UserAssetCardSpend(userId, category, amount);
    }

    public static UserAssetCardSpend create(final Long userId, final SpendingCategory category, final long amount) {
        return create(userId, category, Long.valueOf(amount));
    }

    private void validate(final Long userId, final SpendingCategory category, final Long amount) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (category == null || !category.isUserSelectable()) {
            throw new IllegalArgumentException("허용되지 않은 카드 지출 카테고리입니다.");
        }
        if (amount == null || amount < 0) {
            throw new IllegalArgumentException("카드 지출 금액은 0 이상이어야 합니다.");
        }
    }
}
