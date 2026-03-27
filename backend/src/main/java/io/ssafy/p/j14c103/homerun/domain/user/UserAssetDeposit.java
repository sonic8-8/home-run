package io.ssafy.p.j14c103.homerun.domain.user;

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
@Table(name = "user_asset_deposits")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAssetDeposit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_asset_deposit_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private UserAssetDeposit(final Long userId, final String name, final Integer amount) {
        validate(userId, name, amount);
        this.userId = userId;
        this.name = name;
        this.amount = amount;
        this.createdAt = LocalDateTime.now();
    }

    public static UserAssetDeposit create(final Long userId, final String name, final Integer amount) {
        return new UserAssetDeposit(userId, name, amount);
    }

    private void validate(final Long userId, final String name, final Integer amount) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("예금 항목명은 필수입니다.");
        }
        if (amount == null || amount < 0) {
            throw new IllegalArgumentException("예금 금액은 0 이상이어야 합니다.");
        }
    }
}
