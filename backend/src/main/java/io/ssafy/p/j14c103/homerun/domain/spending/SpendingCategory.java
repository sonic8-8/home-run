package io.ssafy.p.j14c103.homerun.domain.spending;

import java.util.Arrays;

public enum SpendingCategory {

    FUEL("주유"),
    MART("대형마트"),
    TRANSPORT("교통"),
    EDUCATION("교육/육아"),
    TELECOM("통신"),
    OVERSEAS("해외"),
    LIVING("생활"),
    TRANSFER("이체");

    private final String displayName;

    SpendingCategory(final String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static SpendingCategory from(final String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            return TRANSFER;
        }
        return Arrays.stream(values())
                .filter(c -> c.displayName.equals(categoryName))
                .findFirst()
                .orElse(TRANSFER);
    }
}
