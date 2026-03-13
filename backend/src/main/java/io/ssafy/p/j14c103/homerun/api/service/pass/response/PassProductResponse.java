package io.ssafy.p.j14c103.homerun.api.service.pass.response;

import io.ssafy.p.j14c103.homerun.domain.pass.PassProduct;

public class PassProductResponse {

    private final Long id;
    private final String name;
    private final String description;

    private PassProductResponse(final Long id, final String name, final String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public static PassProductResponse from(final PassProduct product) {
        if (product == null) {
            throw new IllegalArgumentException("PASS 상품은 null일 수 없습니다.");
        }
        return new PassProductResponse(product.getId(), product.getName(), product.getDescription());
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
}
