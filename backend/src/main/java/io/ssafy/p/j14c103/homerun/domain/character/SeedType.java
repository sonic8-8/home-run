package io.ssafy.p.j14c103.homerun.domain.character;

import java.util.Arrays;

public enum SeedType {
    MY_DATA,
    PROFILE,
    MANUAL;

    public static SeedType from(final String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("seedType은 비어 있을 수 없습니다.");
        }

        return Arrays.stream(values())
            .filter(seedType -> seedType.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 seedType입니다: " + value));
    }
}
