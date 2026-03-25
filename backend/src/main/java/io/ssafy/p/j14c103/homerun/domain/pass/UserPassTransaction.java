package io.ssafy.p.j14c103.homerun.domain.pass;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "유저패스거래내역")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPassTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "유저패스거래내역번호")
    private Long id;

    @Column(name = "유저구독패스번호", nullable = false)
    private Long subscriptionId;

    @Column(name = "거래금액")
    private Integer amount;

    @Column(name = "거래일시")
    private String transactionDate;

    private UserPassTransaction(final Long subscriptionId, final Integer amount) {
        this.subscriptionId = subscriptionId;
        this.amount = amount;
        this.transactionDate = LocalDateTime.now().toString();
    }

    public static UserPassTransaction create(final Long subscriptionId, final Integer amount) {
        if (subscriptionId == null) {
            throw new IllegalArgumentException("구독 ID는 필수입니다.");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("거래 금액은 0보다 커야 합니다.");
        }
        return new UserPassTransaction(subscriptionId, amount);
    }
}
