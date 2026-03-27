package io.ssafy.p.j14c103.homerun.domain.spending;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public enum SpendingCategory {

    FUEL("주유"),
    MART("대형마트"),
    TRANSPORT("교통"),
    EDUCATION("교육/육아"),
    TELECOM("통신"),
    OVERSEAS("해외"),
    LIVING("생활"),
    FIXED_EXPENSE("고정지출"),
    TRANSFER("이체");

    private final String displayName;

    SpendingCategory(final String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isUserSelectable() {
        return this != TRANSFER && this != FIXED_EXPENSE;
    }

    public static SpendingCategory fromCode(final String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("소비 카테고리 코드는 필수입니다.");
        }

        return Arrays.stream(values())
                .filter(category -> category.name().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("허용되지 않은 소비 카테고리 코드입니다."));
    }

    public static Optional<SpendingCategory> fromStoredCode(final String storedCode) {
        if (storedCode == null || storedCode.isBlank()) {
            return Optional.empty();
        }

        final String normalized = storedCode.trim().toUpperCase(Locale.ROOT);
        try {
            final SpendingCategory category = valueOf(normalized);
            if (category == TRANSFER) {
                return Optional.empty();
            }
            return Optional.of(category);
        } catch (final IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    public static List<SpendingCategory> fromStoredCodes(final String storedCodes) {
        if (storedCodes == null || storedCodes.isBlank()) {
            return List.of();
        }

        return Arrays.stream(storedCodes.split(","))
                .map(SpendingCategory::fromStoredCode)
                .flatMap(Optional::stream)
                .distinct()
                .toList();
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
