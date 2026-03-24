package io.ssafy.p.j14c103.homerun.api.service.pass.response;

import io.ssafy.p.j14c103.homerun.domain.pass.PassProduct;

public class PassProductResponse {

    private final Long passId;
    private final String name;
    private final Integer amountPerSave;
    private final String description;

    private PassProductResponse(final Long passId, final String name,
                                final Integer amountPerSave, final String description) {
        this.passId = passId;
        this.name = name;
        this.amountPerSave = amountPerSave;
        this.description = description;
    }

    public static PassProductResponse from(final PassProduct product) {
        if (product == null) {
            throw new IllegalArgumentException("PASS 상품은 null일 수 없습니다.");
        }
        return new PassProductResponse(
                product.getId(), product.getName(),
                product.getAmountPerSave(), product.getDescription());
    }

    public Long getPassId() { return passId; }
    public String getName() { return name; }
    public Integer getAmountPerSave() { return amountPerSave; }
    public String getDescription() { return description; }
}
