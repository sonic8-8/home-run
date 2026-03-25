package io.ssafy.p.j14c103.homerun.api.service.character.career;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import io.ssafy.p.j14c103.homerun.api.service.character.career.request.JobOfferQueryRequest;
import io.ssafy.p.j14c103.homerun.api.service.character.career.response.JobOfferQueryResponse;
import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareer;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class JobOfferQueryServiceTest {

    @Autowired
    private JobOfferQueryService jobOfferQueryService;

    @DisplayName("game-core가 사용할 이직 오퍼 계약을 반환한다.")
    @Test
    void getJobOffers() {
        // given
        final JobOfferQueryRequest request = JobOfferQueryRequest.of(
            createCareer(JobType.SMALL_BIZ, 30_000_000),
            createStat(100),
            2
        );

        // when
        final JobOfferQueryResponse response = jobOfferQueryService.getJobOffers(request);

        // then
        assertThat(response.offerChanceBonusRate()).isEqualTo(10);
        assertThat(response.meetFriendBonusApplied()).isTrue();
        assertThat(response.offers())
            .extracting(
                JobOfferQueryResponse.JobOfferResponse::offerId,
                JobOfferQueryResponse.JobOfferResponse::jobType,
                JobOfferQueryResponse.JobOfferResponse::displayCompanyName,
                JobOfferQueryResponse.JobOfferResponse::currentSalary,
                JobOfferQueryResponse.JobOfferResponse::offeredSalary,
                JobOfferQueryResponse.JobOfferResponse::probationTurns
            )
            .containsExactly(
                tuple("OFFER-001", JobType.SMALL_BIZ, "OO 중소기업", 30_000_000, 45_000_000, 1),
                tuple("OFFER-002", JobType.MID_BIZ, "OO 중견기업", 30_000_000, 45_000_000, 2),
                tuple("OFFER-003", JobType.STARTUP, "OO 스타트업", 30_000_000, 45_000_000, 2),
                tuple("OFFER-004", JobType.LARGE_BIZ, "OO 대기업", 30_000_000, 45_000_000, 2),
                tuple("OFFER-005", JobType.FREELANCER, "OO 프리랜서 프로젝트", 30_000_000, 45_000_000, null)
            );
    }

    private GameCareer createCareer(final JobType jobType, final int salary) {
        return GameCareer.builder()
            .gameId(1)
            .jobType(jobType)
            .jobTitle("사원")
            .salary(salary)
            .tenureTurns(12)
            .recentStudyCount(0)
            .recentNetworkingCount(0)
            .negotiationPreparationScore(0)
            .lastNegotiatedTurn(0)
            .employmentStatus(EmploymentStatus.EMPLOYED)
            .probationEndTurn(null)
            .rehireAvailableTurn(null)
            .remainingUnemploymentBenefitTurns(0)
            .salaryBeforeResignation(null)
            .build();
    }

    private GameStat createStat(final int knowledge) {
        return GameStat.builder()
            .gameId(1)
            .health(70)
            .fatigue(20)
            .stress(20)
            .happiness(50)
            .knowledge(knowledge)
            .burnout(false)
            .burnoutStartedTurn(null)
            .hospitalizedUntilTurn(null)
            .build();
    }
}
