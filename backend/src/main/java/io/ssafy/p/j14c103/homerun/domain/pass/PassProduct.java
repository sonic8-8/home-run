package io.ssafy.p.j14c103.homerun.domain.pass;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "패스")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PassProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "패스번호")
    private Long id;

    @Column(name = "패스이름")
    private String name;

    @Column(name = "기본저축금액")
    private Long amountPerSave;

    @Column(name = "패스설명", columnDefinition = "TEXT")
    private String description;

    private PassProduct(final String name, final Long amountPerSave, final String description) {
        this.name = name;
        this.amountPerSave = amountPerSave;
        this.description = description;
    }

    public static PassProduct create(final String name, final Long amountPerSave, final String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("PASS 상품명은 필수입니다.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("PASS 상품 설명은 필수입니다.");
        }
        return new PassProduct(name, amountPerSave, description);
    }

    public static PassProduct create(final String name, final int amountPerSave, final String description) {
        return create(name, Long.valueOf(amountPerSave), description);
    }
}
