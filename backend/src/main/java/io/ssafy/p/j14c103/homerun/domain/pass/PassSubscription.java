package io.ssafy.p.j14c103.homerun.domain.pass;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "유저구독패스")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PassSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "유저구독패스번호")
    private Long id;

    @Column(name = "유저번호", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "패스번호2", nullable = false)
    private PassProduct passProduct;

    @Column(name = "구독패스명")
    private String subscriptionName;

    @Column(name = "회당저축금액")
    private Integer savingAmount;

    @Column(name = "출금계좌참조")
    private String sourceAccountNo;

    @Column(name = "활성여부")
    private Boolean isActive;

    @Column(name = "가입일자")
    private LocalDateTime subscribedAt;

    @Column(name = "해지일시")
    private LocalDateTime canceledAt;

    private PassSubscription(
            final Long userId,
            final PassProduct passProduct,
            final Integer savingAmount,
            final String sourceAccountNo) {
        this.userId = userId;
        this.passProduct = passProduct;
        this.subscriptionName = passProduct.getName();
        this.savingAmount = savingAmount;
        this.sourceAccountNo = sourceAccountNo;
        this.isActive = true;
        this.subscribedAt = LocalDateTime.now();
    }

    public static PassSubscription create(
            final Long userId,
            final PassProduct passProduct,
            final Integer savingAmount,
            final String sourceAccountNo) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (passProduct == null) {
            throw new IllegalArgumentException("PASS 상품은 필수입니다.");
        }
        if (savingAmount == null || savingAmount <= 0) {
            throw new IllegalArgumentException("1회 저축 금액은 0보다 커야 합니다.");
        }
        if (sourceAccountNo == null || sourceAccountNo.isBlank()) {
            throw new IllegalArgumentException("출금 계좌번호는 필수입니다.");
        }
        return new PassSubscription(userId, passProduct, savingAmount, sourceAccountNo);
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
