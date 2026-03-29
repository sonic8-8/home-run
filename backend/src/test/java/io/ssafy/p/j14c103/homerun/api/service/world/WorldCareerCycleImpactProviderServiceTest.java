package io.ssafy.p.j14c103.homerun.api.service.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.world.response.CareerCycleImpactProviderResponse;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareer;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareerRepository;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CycleState;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CycleType;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import io.ssafy.p.j14c103.homerun.support.IntegrationTestSupport;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class WorldCareerCycleImpactProviderServiceTest extends IntegrationTestSupport {

    @Autowired
    private WorldCareerCycleImpactProviderService worldCareerCycleImpactProviderService;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private GameCareerRepository gameCareerRepository;

    @AfterEach
    void tearDown() {
        gameCareerRepository.deleteAllInBatch();
        gameSessionRepository.deleteAllInBatch();
    }

    @DisplayName("세션의 cycleType과 jobType을 읽어 현재 플레이어 직업 영향 1건을 반환하고 실제 연봉 상태는 gameCareer에 유지한다")
    @Test
    void getCareerCycleImpact() {
        // given
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(JobType.STARTUP, CycleState.of(
                CyclePhase.CRISIS,
                CycleType.CYCLE_CURRENCY_CRISIS,
                8
            ))
        );
        gameCareerRepository.saveAndFlush(createGameCareer(gameSession, JobType.STARTUP, 31_000_000));

        // when
        final CareerCycleImpactProviderResponse response =
            worldCareerCycleImpactProviderService.getCareerCycleImpact(gameSession.getGameSessionId());

        // then
        assertThat(response.getCycleType()).isEqualTo(CycleType.CYCLE_CURRENCY_CRISIS);
        assertThat(response.getJobType()).isEqualTo(JobType.STARTUP);
        assertThat(response.getJobImpact().getSalaryMultiplier()).isEqualByComparingTo("0.5");
        assertThat(response.getJobImpact().getLayoffMultiplier()).isEqualByComparingTo("2.5");
        assertThat(response.getJobImpact().getRehirePenaltyTurns()).isEqualTo(2);
        assertThat(gameCareerRepository.findById(gameSession.getGameSessionId().intValue())
            .orElseThrow()
            .getSalary()).isEqualTo(31_000_000);
    }

    @DisplayName("프리랜서 영향 조회는 layoffMultiplier를 null로 반환한다")
    @Test
    void getCareerCycleImpactForFreelancer() {
        // given
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(JobType.FREELANCER, CycleState.of(
                CyclePhase.CRISIS,
                CycleType.CYCLE_GREAT_DEPRESSION,
                12
            ))
        );
        gameCareerRepository.saveAndFlush(createGameCareer(gameSession, JobType.FREELANCER, 28_000_000));

        // when
        final CareerCycleImpactProviderResponse response =
            worldCareerCycleImpactProviderService.getCareerCycleImpact(gameSession.getGameSessionId());

        // then
        assertThat(response.getCycleType()).isEqualTo(CycleType.CYCLE_GREAT_DEPRESSION);
        assertThat(response.getJobType()).isEqualTo(JobType.FREELANCER);
        assertThat(response.getJobImpact().getSalaryMultiplier()).isEqualByComparingTo("0.2");
        assertThat(response.getJobImpact().getLayoffMultiplier()).isNull();
        assertThat(response.getJobImpact().getRehirePenaltyTurns()).isEqualTo(4);
    }

    @DisplayName("존재하지 않는 세션이면 world 세션 조회 에러를 던진다")
    @Test
    void getCareerCycleImpactWithUnknownSession() {
        // given
        final Long unknownSessionId = 9999L;

        // when
        // then
        assertThatThrownBy(() -> worldCareerCycleImpactProviderService.getCareerCycleImpact(
            unknownSessionId
        ))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.WORLD_SESSION_NOT_FOUND);
    }

    @DisplayName("세션이 phase-only 상태면 world cycle 상태 예외를 던진다")
    @Test
    void getCareerCycleImpactWithPhaseOnlySession() {
        // given
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createPhaseOnlyGameSession(JobType.SMALL_BIZ, CyclePhase.BOOM)
        );
        gameCareerRepository.saveAndFlush(createGameCareer(gameSession, JobType.SMALL_BIZ, 30_000_000));

        // when
        // then
        assertThatThrownBy(() -> worldCareerCycleImpactProviderService.getCareerCycleImpact(
            gameSession.getGameSessionId()
        ))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.WORLD_CYCLE_STATE_INVALID);
    }

    private GameSession createGameSession(
        final JobType jobType,
        final CycleState cycleState
    ) {
        final GameSession gameSession = GameSession.create(
            1L,
            1,
            "윤서",
            CharacterType.FEMALE,
            jobType,
            HousingType.STUDIO,
            "11",
            "11680",
            101L,
            DataSourceType.PROFILE
        );
        gameSession.initializeCapital(
            Money.of(2_000_000L),
            Money.of(2_000_000L),
            LocalDate.of(2026, 1, 1),
            cycleState
        );
        gameSession.advanceTurn(
            12,
            LocalDate.of(2026, 1, 1),
            Money.of(2_000_000L),
            Money.of(2_000_000L),
            cycleState
        );
        return gameSession;
    }

    private GameSession createPhaseOnlyGameSession(
        final JobType jobType,
        final CyclePhase cyclePhase
    ) {
        final GameSession gameSession = GameSession.create(
            1L,
            1,
            "윤서",
            CharacterType.FEMALE,
            jobType,
            HousingType.STUDIO,
            "11",
            "11680",
            101L,
            DataSourceType.PROFILE
        );
        gameSession.initializeCapital(
            Money.of(2_000_000L),
            Money.of(2_000_000L),
            LocalDate.of(2026, 1, 1),
            cyclePhase
        );
        gameSession.advanceTurn(
            12,
            LocalDate.of(2026, 1, 1),
            Money.of(2_000_000L),
            Money.of(2_000_000L),
            cyclePhase
        );
        return gameSession;
    }

    private GameCareer createGameCareer(
        final GameSession gameSession,
        final JobType jobType,
        final int salary
    ) {
        return GameCareer.builder()
            .gameId(gameSession.getGameSessionId().intValue())
            .jobType(jobType)
            .jobTitle("사원")
            .salary(salary)
            .tenureTurns(24)
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
}
