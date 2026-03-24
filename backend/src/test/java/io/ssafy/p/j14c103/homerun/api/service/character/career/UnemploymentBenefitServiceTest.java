package io.ssafy.p.j14c103.homerun.api.service.character.career;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.api.service.character.career.request.UnemploymentBenefitServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.character.career.response.UnemploymentBenefitServiceResponse;
import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareer;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class UnemploymentBenefitServiceTest {

    @Autowired
    private UnemploymentBenefitService unemploymentBenefitService;

    @DisplayName("실업 급여는 최대 3턴까지만 지급된다.")
    @Test
    void consume() {
        // given
        final GameCareer gameCareer = createUnemployedCareer();
        final UnemploymentBenefitServiceRequest request = UnemploymentBenefitServiceRequest.of(
            gameCareer
        );

        // when
        final UnemploymentBenefitServiceResponse firstResponse =
            unemploymentBenefitService.consume(request);
        final UnemploymentBenefitServiceResponse secondResponse =
            unemploymentBenefitService.consume(request);
        final UnemploymentBenefitServiceResponse thirdResponse =
            unemploymentBenefitService.consume(request);
        final UnemploymentBenefitServiceResponse fourthResponse =
            unemploymentBenefitService.consume(request);

        // then
        assertThat(firstResponse.benefitGranted()).isTrue();
        assertThat(firstResponse.benefitAmount()).isEqualTo(1_500_000);
        assertThat(firstResponse.remainingUnemploymentBenefitTurns()).isEqualTo(2);
        assertThat(secondResponse.remainingUnemploymentBenefitTurns()).isEqualTo(1);
        assertThat(thirdResponse.remainingUnemploymentBenefitTurns()).isZero();
        assertThat(fourthResponse.benefitGranted()).isFalse();
        assertThat(fourthResponse.benefitAmount()).isZero();
        assertThat(fourthResponse.remainingUnemploymentBenefitTurns()).isZero();
        assertThat(fourthResponse.message()).isEqualTo("실업 급여 지급 대상이 아닙니다.");
    }

    private GameCareer createUnemployedCareer() {
        return GameCareer.builder()
            .gameId(1)
            .jobType(JobType.MID_BIZ)
            .jobTitle("사원")
            .salary(36_000_000)
            .tenureTurns(10)
            .recentStudyCount(0)
            .recentNetworkingCount(0)
            .negotiationPreparationScore(0)
            .lastNegotiatedTurn(0)
            .employmentStatus(EmploymentStatus.UNEMPLOYED)
            .probationEndTurn(null)
            .rehireAvailableTurn(12)
            .remainingUnemploymentBenefitTurns(3)
            .salaryBeforeResignation(36_000_000)
            .build();
    }
}
