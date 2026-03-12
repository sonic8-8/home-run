package io.ssafy.p.j14c103.homerun.domain.pass;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "pass_subscriptions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PassSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pass_product_id", nullable = false)
    private PassProduct passProduct;

    @Column(nullable = false)
    private String sourceAccountNo;

    @Column(nullable = false)
    private boolean isActive;

    @Column(nullable = false)
    private LocalDateTime subscribedAt;

    private LocalDateTime canceledAt;

    private PassSubscription(
            final Long userId,
            final PassProduct passProduct,
            final String sourceAccountNo) {
        this.userId = userId;
        this.passProduct = passProduct;
        this.sourceAccountNo = sourceAccountNo;
        this.isActive = true;
        this.subscribedAt = LocalDateTime.now();
    }

    public static PassSubscription create(
            final Long userId,
            final PassProduct passProduct,
            final String sourceAccountNo) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (passProduct == null) {
            throw new IllegalArgumentException("PASS 상품은 필수입니다.");
        }
        if (sourceAccountNo == null || sourceAccountNo.isBlank()) {
            throw new IllegalArgumentException("출금 계좌번호는 필수입니다.");
        }
        return new PassSubscription(userId, passProduct, sourceAccountNo);
    }

    public void cancel() {
        if (!this.isActive) {
            throw new IllegalStateException("이미 해지된 구독입니다.");
        }
        this.isActive = false;
        this.canceledAt = LocalDateTime.now();
    }

    public String getPassName() {
        return passProduct.getName();
    }
}
