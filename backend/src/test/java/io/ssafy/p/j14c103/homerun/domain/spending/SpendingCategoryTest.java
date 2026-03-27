package io.ssafy.p.j14c103.homerun.domain.spending;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class SpendingCategoryTest {

    @DisplayName("SSAFY 카테고리명으로 SpendingCategory를 찾을 수 있다")
    @ParameterizedTest
    @CsvSource({
            "주유, FUEL",
            "대형마트, MART",
            "교통, TRANSPORT",
            "교육/육아, EDUCATION",
            "통신, TELECOM",
            "해외, OVERSEAS",
            "생활, LIVING"
    })
    void from_ssafyCategoryName(final String categoryName, final SpendingCategory expected) {
        assertThat(SpendingCategory.from(categoryName)).isEqualTo(expected);
    }

    @DisplayName("알 수 없는 카테고리명은 TRANSFER를 반환한다")
    @Test
    void from_unknown_returnsTransfer() {
        assertThat(SpendingCategory.from("알 수 없는 카테고리")).isEqualTo(SpendingCategory.TRANSFER);
    }

    @DisplayName("null은 TRANSFER를 반환한다")
    @Test
    void from_null_returnsTransfer() {
        assertThat(SpendingCategory.from(null)).isEqualTo(SpendingCategory.TRANSFER);
    }

    @DisplayName("빈 문자열은 TRANSFER를 반환한다")
    @Test
    void from_blank_returnsTransfer() {
        assertThat(SpendingCategory.from("  ")).isEqualTo(SpendingCategory.TRANSFER);
    }

    @DisplayName("각 카테고리는 한글 displayName을 반환한다")
    @Test
    void getDisplayName() {
        assertThat(SpendingCategory.FUEL.getDisplayName()).isEqualTo("주유");
        assertThat(SpendingCategory.TRANSFER.getDisplayName()).isEqualTo("이체");
    }

    @DisplayName("저장된 코드값으로 추천 카테고리를 찾을 수 있다")
    @ParameterizedTest
    @CsvSource({
            "LIVING, LIVING",
            "transport, TRANSPORT",
            " TELECOM , TELECOM"
    })
    void fromStoredCode(final String storedCode, final SpendingCategory expected) {
        assertThat(SpendingCategory.fromStoredCode(storedCode)).contains(expected);
    }

    @DisplayName("저장된 코드값이 TRANSFER면 추천 카테고리에서 제외된다")
    @Test
    void fromStoredCode_transferReturnsEmpty() {
        assertThat(SpendingCategory.fromStoredCode("TRANSFER")).isEmpty();
    }

    @DisplayName("저장된 코드 목록은 유효한 추천 카테고리만 중복 없이 반환한다")
    @Test
    void fromStoredCodes_filtersUnknownAndDuplicateCodes() {
        assertThat(SpendingCategory.fromStoredCodes("LIVING,UNKNOWN,TRANSPORT,LIVING,TRANSFER"))
                .containsExactly(SpendingCategory.LIVING, SpendingCategory.TRANSPORT);
    }

    @DisplayName("저장된 코드 목록이 비어 있으면 빈 목록을 반환한다")
    @Test
    void fromStoredCodes_blankReturnsEmptyList() {
        assertThat(SpendingCategory.fromStoredCodes("  ")).isEqualTo(List.of());
    }
}
