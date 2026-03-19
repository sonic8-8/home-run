package io.ssafy.p.j14c103.homerun.domain.character;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.Arrays;

public enum SeedType {
    MY_DATA,
    PROFILE,
    MANUAL;

    public static SeedType from(final String value) {
        if (value == null || value.isBlank()) {
            throw new HomerunException(ErrorCode.CHARACTER_SEED_TYPE_REQUIRED);
        }

        return Arrays.stream(values())
            .filter(seedType -> seedType.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new HomerunException(ErrorCode.CHARACTER_SEED_TYPE_UNSUPPORTED));
    }
}
