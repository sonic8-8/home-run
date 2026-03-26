package io.ssafy.p.j14c103.homerun.domain.world.housing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ContractReviewPolicyTest {

    private final ContractReviewPolicy contractReviewPolicy = new ContractReviewPolicy();

    @DisplayName("actual trap을 모두 정확히 체크하면 SAFE와 PASSED를 반환한다.")
    @Test
    void calculateSafe() {
        // when
        final ContractReviewCalculationResult result = contractReviewPolicy.calculate(
            List.of("TRAP-01", "TRAP-02"),
            List.of("TRAP-01", "TRAP-02", "TRAP-03"),
            List.of(
                createTrap("TRAP-01", "-5000000", 20),
                createTrap("TRAP-02", "-1000000", 10)
            )
        );

        // then
        assertThat(result.getContractResult()).isEqualTo(ContractResult.SAFE);
        assertThat(result.getReviewStatus()).isEqualTo(ContractReviewStatus.PASSED);
        assertThat(result.getDetectedTraps()).containsExactly("TRAP-01", "TRAP-02");
        assertThat(result.getTrapsDetected()).isEqualTo(2);
        assertThat(result.getTrapsCorrectlyIdentified()).isEqualTo(2);
    }

    @DisplayName("non-critical trap 일부를 놓치면 WARNING과 PASSED를 반환한다.")
    @Test
    void calculateWarningWhenMissingNonCriticalTrap() {
        // when
        final ContractReviewCalculationResult result = contractReviewPolicy.calculate(
            List.of("TRAP-01"),
            List.of("TRAP-01", "TRAP-02"),
            List.of(
                createTrap("TRAP-01", "-5000000", 20),
                createTrap("TRAP-02", "-1000000", 10)
            )
        );

        // then
        assertThat(result.getContractResult()).isEqualTo(ContractResult.WARNING);
        assertThat(result.getReviewStatus()).isEqualTo(ContractReviewStatus.PASSED);
        assertThat(result.getDetectedTraps()).containsExactly("TRAP-01");
        assertThat(result.getMatchedActualTraps()).hasSize(1);
    }

    @DisplayName("critical trap을 놓치면 FAIL과 FAILED를 반환한다.")
    @Test
    void calculateFailWhenMissingCriticalTrap() {
        // when
        final ContractReviewCalculationResult result = contractReviewPolicy.calculate(
            List.of("TRAP-01"),
            List.of("TRAP-01", "TRAP-02"),
            List.of(
                createTrap("TRAP-01", "-5000000", 20),
                createTrap("TRAP-02", "ALL_DEPOSIT_LOST", 50)
            )
        );

        // then
        assertThat(result.getContractResult()).isEqualTo(ContractResult.FAIL);
        assertThat(result.getReviewStatus()).isEqualTo(ContractReviewStatus.FAILED);
        assertThat(result.getDetectedTraps()).containsExactly("TRAP-01");
    }

    @DisplayName("false positive가 있어도 critical trap을 모두 찾았으면 WARNING과 PASSED를 반환한다.")
    @Test
    void calculateWarningWhenFalsePositiveExists() {
        // when
        final ContractReviewCalculationResult result = contractReviewPolicy.calculate(
            List.of("TRAP-01", "TRAP-03"),
            List.of("TRAP-01", "TRAP-02", "TRAP-03"),
            List.of(
                createTrap("TRAP-01", "ALL_DEPOSIT_LOST", 50),
                createTrap("TRAP-02", "-1000000", 10)
            )
        );

        // then
        assertThat(result.getContractResult()).isEqualTo(ContractResult.WARNING);
        assertThat(result.getReviewStatus()).isEqualTo(ContractReviewStatus.PASSED);
        assertThat(result.getDetectedTraps()).containsExactly("TRAP-01");
        assertThat(result.getTrapsDetected()).isEqualTo(1);
    }

    @DisplayName("actual trap이 없고 checkedTraps도 비어 있으면 SAFE와 PASSED를 반환한다.")
    @Test
    void calculateSafeWhenNoActualTrapExists() {
        // when
        final ContractReviewCalculationResult result = contractReviewPolicy.calculate(
            List.of(),
            List.of("TRAP-01", "TRAP-02"),
            List.of()
        );

        // then
        assertThat(result.getContractResult()).isEqualTo(ContractResult.SAFE);
        assertThat(result.getReviewStatus()).isEqualTo(ContractReviewStatus.PASSED);
        assertThat(result.getDetectedTraps()).isEmpty();
    }

    @DisplayName("duplicate trapId를 제출하면 INVALID_INPUT_VALUE 예외가 발생한다.")
    @Test
    void calculateWithDuplicateCheckedTrap() {
        // when & then
        assertThatThrownBy(() -> contractReviewPolicy.calculate(
            List.of("TRAP-01", "TRAP-01"),
            List.of("TRAP-01", "TRAP-02"),
            List.of(createTrap("TRAP-01", "-5000000", 20))
        ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @DisplayName("노출되지 않은 trapId를 제출하면 INVALID_INPUT_VALUE 예외가 발생한다.")
    @Test
    void calculateWithUnknownCheckedTrap() {
        // when & then
        assertThatThrownBy(() -> contractReviewPolicy.calculate(
            List.of("TRAP-99"),
            List.of("TRAP-01", "TRAP-02"),
            List.of(createTrap("TRAP-01", "-5000000", 20))
        ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    private ContractTrap createTrap(
        final String trapId,
        final String cash,
        final int stress
    ) {
        return ContractTrap.create(
            trapId,
            "TYPE",
            "REGISTRY",
            "설명",
            ContractTrapPenalty.create(cash, stress)
        );
    }
}
