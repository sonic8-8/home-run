package io.ssafy.p.j14c103.homerun.domain.pass;

import io.ssafy.p.j14c103.homerun.domain.money.Money;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "pass_products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PassProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal amountPerSave;

    @Column(nullable = false)
    private String description;

    private PassProduct(final String name, final BigDecimal amountPerSave, final String description) {
        this.name = name;
        this.amountPerSave = amountPerSave;
        this.description = description;
    }

    public static PassProduct create(final String name, final Money amountPerSave, final String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("PASS 상품명은 필수입니다.");
        }
        if (amountPerSave == null || amountPerSave.isZero() || amountPerSave.isNegative()) {
            throw new IllegalArgumentException("1회 저축 금액은 0보다 커야 합니다.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("PASS 상품 설명은 필수입니다.");
        }
        return new PassProduct(name, amountPerSave.getAmount(), description);
    }

    public Money getAmountPerSaveMoney() {
        return Money.of(amountPerSave);
    }
}
