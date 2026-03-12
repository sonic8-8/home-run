package io.ssafy.p.j14c103.homerun.api.service.pass.response;

import io.ssafy.p.j14c103.homerun.domain.pass.PassProduct;

import java.math.BigDecimal;

public class PassProductResponse {

    private final Long id;
    private final String name;
    private final BigDecimal amountPerSave;
    private final String description;

    private PassProductResponse(
            final Long id,
            final String name,
            final BigDecimal amountPerSave,
            final String description) {
        this.id = id;
        this.name = name;
        this.amountPerSave = amountPerSave;
        this.description = description;
    }

    public static PassProductResponse from(final PassProduct product) {
        if (product == null) {
            throw new IllegalArgumentException("PASS 상품은 null일 수 없습니다.");
        }
        return new PassProductResponse(
                product.getId(),
                product.getName(),
                product.getAmountPerSave(),
                product.getDescription());
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getAmountPerSave() {
        return amountPerSave;
    }

    public String getDescription() {
        return description;
    }
}
