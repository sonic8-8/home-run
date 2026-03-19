package io.ssafy.p.j14c103.homerun.domain.character.schedule;

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

    private final SideJobIncomePolicy policy = new SideJobIncomePolicy();

    @DisplayName("지식 구간에 따라 부업 수입 범위가 달라진다.")
    @ParameterizedTest(name = "[{index}] knowledge={0}")
    @MethodSource("incomeRanges")
    void resolveRange(
        final int knowledge,
        final String sideJobKind,
        final int minimumIncome,
        final int maximumIncome
    ) {
        // when
        final SideJobIncomePolicy.IncomeRange result = policy.resolveRange(knowledge);

        // then
        assertThat(result.sideJobKind()).isEqualTo(sideJobKind);
        assertThat(result.minimumIncome()).isEqualTo(minimumIncome);
        assertThat(result.maximumIncome()).isEqualTo(maximumIncome);
    }

    @DisplayName("저지식 구간 preview는 부업 수입 범위를 제공한다.")
    @ParameterizedTest(name = "[{index}] knowledge={0}")
    @MethodSource("previewRanges")
    void resolvePreviewRange(
        final int knowledge,
        final int minimumIncome,
        final int maximumIncome
    ) {
        // when
        final SideJobIncomePolicy.IncomePreview result = policy.resolvePreview(knowledge);

        // then
        assertThat(result.minimumIncome()).isEqualTo(minimumIncome);
        assertThat(result.maximumIncome()).isEqualTo(maximumIncome);
        assertThat(result.isRange()).isTrue();
    }

    @DisplayName("지식이 범위를 벗어나면 예외가 발생한다.")
    @Test
    void resolveRangeWithInvalidKnowledge() {
        // when & then
        assertThatThrownBy(() -> policy.resolveRange(101))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.CHARACTER_STAT_INVALID);
    }

    @DisplayName("부업 정책 구성이 잘못되면 에러코드 기반 예외가 발생한다.")
    @Test
    void createWithInvalidPolicy() {
        assertThatThrownBy(() -> new SideJobIncomePolicy(null))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.CHARACTER_SCHEDULE_POLICY_INVALID);
    }

    @DisplayName("중고지식 구간 preview는 실제 부업 수입과 같은 결정형 금액을 사용한다.")
    @ParameterizedTest(name = "[{index}] knowledge={0}")
    @MethodSource("deterministicIncomes")
    void resolveDeterministicPreview(final int knowledge, final int expectedIncome) {
        // when
        final SideJobIncomePolicy.IncomePreview result = policy.resolvePreview(knowledge);

        // then
        assertThat(result.minimumIncome()).isEqualTo(expectedIncome);
        assertThat(result.maximumIncome()).isEqualTo(expectedIncome);
        assertThat(result.isRange()).isFalse();
    }

    @DisplayName("저지식 구간 실제 부업 수입은 허용 범위 안에서 랜덤하게 계산된다.")
    @ParameterizedTest(name = "[{index}] knowledge={0}")
    @MethodSource("randomIncomes")
    void calculateRandomIncome(
        final int knowledge,
        final int randomizedIncome
    ) {
        // given
        final SideJobIncomePolicy randomPolicy = new SideJobIncomePolicy(
            (minimumIncome, maximumIncome) -> randomizedIncome
        );

        // when
        final int result = randomPolicy.calculateIncome(knowledge);

        // then
        assertThat(result).isEqualTo(randomizedIncome);
        assertThat(randomPolicy.resolveRange(knowledge).contains(result)).isTrue();
    }

    @DisplayName("중고지식 구간 실제 부업 수입은 지식 값에 따라 결정형으로 계산된다.")
    @ParameterizedTest(name = "[{index}] knowledge={0}")
    @MethodSource("deterministicIncomes")
    void calculateDeterministicIncome(final int knowledge, final int expectedIncome) {
        // when
        final int result = policy.calculateIncome(knowledge);

        // then
        assertThat(result).isEqualTo(expectedIncome);
        assertThat(policy.resolveRange(knowledge).contains(result)).isTrue();
    }

    private static Stream<Arguments> incomeRanges() {
        return Stream.of(
            Arguments.of(0, "배달·단순 알바", 200_000, 350_000),
            Arguments.of(30, "배달·단순 알바", 200_000, 350_000),
            Arguments.of(31, "과외·프리랜서", 300_000, 500_000),
            Arguments.of(60, "과외·프리랜서", 300_000, 500_000),
            Arguments.of(61, "컨설팅·강의", 600_000, 1_000_000),
            Arguments.of(100, "컨설팅·강의", 600_000, 1_000_000)
        );
    }

    private static Stream<Arguments> previewRanges() {
        return Stream.of(
            Arguments.of(0, 200_000, 350_000),
            Arguments.of(10, 200_000, 350_000),
            Arguments.of(30, 200_000, 350_000)
        );
    }

    private static Stream<Arguments> randomIncomes() {
        return Stream.of(
            Arguments.of(0, 200_000),
            Arguments.of(10, 250_000),
            Arguments.of(30, 350_000)
        );
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
