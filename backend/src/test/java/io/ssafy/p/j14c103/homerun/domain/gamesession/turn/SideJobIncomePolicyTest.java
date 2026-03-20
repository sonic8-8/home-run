package io.ssafy.p.j14c103.homerun.domain.gamesession.turn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SideJobIncomePolicyTest {

    private final SideJobIncomePolicy sideJobIncomePolicy = new SideJobIncomePolicy();

    @DisplayName("저지식 구간 부업 preview는 범위를 반환한다.")
    @ParameterizedTest(name = "[{index}] knowledge={0}")
    @MethodSource("lowKnowledgeRanges")
    void previewIncomeForLowKnowledge(
        final int knowledge,
        final int minAmount,
        final int maxAmount
    ) {
        // when
        final SideJobIncomePolicy.SideJobIncomePreview preview = sideJobIncomePolicy.previewIncome(
            knowledge
        );

        // then
        assertThat(preview.minAmount()).isEqualTo(minAmount);
        assertThat(preview.maxAmount()).isEqualTo(maxAmount);
        assertThat(preview.rangePreview()).isTrue();
    }

    @DisplayName("중고지식 구간 부업 preview는 결정형 금액을 반환한다.")
    @ParameterizedTest(name = "[{index}] knowledge={0}")
    @MethodSource("deterministicIncomes")
    void previewIncomeForDeterministicKnowledge(final int knowledge, final int expectedIncome) {
        // when
        final SideJobIncomePolicy.SideJobIncomePreview preview = sideJobIncomePolicy.previewIncome(
            knowledge
        );

        // then
        assertThat(preview.minAmount()).isEqualTo(expectedIncome);
        assertThat(preview.maxAmount()).isEqualTo(expectedIncome);
        assertThat(preview.rangePreview()).isFalse();
    }

    @DisplayName("저지식 구간 실제 부업 수입은 허용 범위 안의 값으로 계산된다.")
    @ParameterizedTest(name = "[{index}] knowledge={0}")
    @MethodSource("lowKnowledgeValues")
    void calculateActualIncomeForLowKnowledge(final int knowledge) {
        // when
        final int income = sideJobIncomePolicy.calculateActualIncome(knowledge);

        // then
        assertThat(income).isBetween(200_000, 350_000);
        assertThat(income % 10_000).isZero();
    }

    @DisplayName("중고지식 구간 실제 부업 수입은 지식 값에 따라 결정형으로 계산된다.")
    @ParameterizedTest(name = "[{index}] knowledge={0}")
    @MethodSource("deterministicIncomes")
    void calculateActualIncomeForDeterministicKnowledge(
        final int knowledge,
        final int expectedIncome
    ) {
        // when
        final int income = sideJobIncomePolicy.calculateActualIncome(knowledge);

        // then
        assertThat(income).isEqualTo(expectedIncome);
    }

    @DisplayName("잘못된 지식 값이면 예외가 발생한다.")
    @Test
    void previewIncomeWithInvalidKnowledge() {
        // given

        // when & then
        assertThatThrownBy(() -> sideJobIncomePolicy.previewIncome(101))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.SCHEDULE_KNOWLEDGE_INVALID);
    }

    private static Stream<Arguments> lowKnowledgeRanges() {
        return Stream.of(
            Arguments.of(0, 200_000, 350_000),
            Arguments.of(10, 200_000, 350_000),
            Arguments.of(30, 200_000, 350_000)
        );
    }

    private static Stream<Integer> lowKnowledgeValues() {
        return Stream.of(0, 15, 30);
    }

    private static Stream<Arguments> deterministicIncomes() {
        return Stream.of(
            Arguments.of(31, 300_000),
            Arguments.of(45, 400_000),
            Arguments.of(50, 430_000),
            Arguments.of(55, 470_000),
            Arguments.of(60, 500_000),
            Arguments.of(61, 600_000),
            Arguments.of(80, 790_000),
            Arguments.of(100, 1_000_000)
        );
    }
}
