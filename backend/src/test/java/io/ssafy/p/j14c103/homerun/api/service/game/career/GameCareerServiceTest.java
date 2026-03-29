package io.ssafy.p.j14c103.homerun.api.service.game.career;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.game.career.request.AcceptJobTransferServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.game.career.response.JobOfferListResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.career.response.JobTransferResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.career.response.SalaryNegotiationResponse;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.domain.character.GameStatRepository;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareer;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareerRepository;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.SessionStatus;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import io.ssafy.p.j14c103.homerun.support.IntegrationTestSupport;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class GameCareerServiceTest extends IntegrationTestSupport {

    @Autowired
    private GameCareerService gameCareerService;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private GameCareerRepository gameCareerRepository;

    @Autowired
    private GameStatRepository gameStatRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        gameCareerRepository.deleteAllInBatch();
        gameStatRepository.deleteAllInBatch();
        gameSessionRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @DisplayName("연봉 협상은 세션 가드 후 커리어 상태를 갱신하고 공개 응답을 반환한다.")
    @Test
    void negotiateSalary() {
        // given
        final User user = saveUser("career-negotiate-user@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), 13, SessionStatus.IN_PROGRESS, CyclePhase.BOOM)
        );
        gameCareerRepository.saveAndFlush(
            createCareer(gameSession.getGameSessionId(), JobType.SMALL_BIZ, 30_000_000, 0, 2)
        );
        gameStatRepository.saveAndFlush(createStat(gameSession.getGameSessionId(), 70, 75));

        // when
        final SalaryNegotiationResponse response = gameCareerService.negotiateSalary(
            user.getId(),
            gameSession.getGameSessionId()
        );

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getPreviousSalary()).isEqualTo(30_000_000);
        assertThat(response.getNewSalary()).isEqualTo(33_300_000);
        assertThat(response.getRaiseRate()).isEqualTo(11);
        assertThat(response.getLastNegotiatedTurn()).isEqualTo(13);

        final GameCareer updatedCareer = gameCareerRepository.findById(
            gameSession.getGameSessionId().intValue()
        ).orElseThrow();
        assertThat(updatedCareer.getSalary()).isEqualTo(33_300_000);
        assertThat(updatedCareer.getLastNegotiatedTurn()).isEqualTo(13);
    }

    @DisplayName("이직 오퍼 목록 조회는 세션 문맥에서 공개 응답으로 변환한다.")
    @Test
    void getJobOffers() {
        // given
        final User user = saveUser("career-job-offer-user@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), 13, SessionStatus.IN_PROGRESS, CyclePhase.RECOVERY)
        );
        gameCareerRepository.saveAndFlush(
            createCareer(gameSession.getGameSessionId(), JobType.SMALL_BIZ, 30_000_000, 0, 2)
        );
        gameStatRepository.saveAndFlush(createStat(gameSession.getGameSessionId(), 70, 100));

        // when
        final JobOfferListResponse response = gameCareerService.getJobOffers(
            user.getId(),
            gameSession.getGameSessionId()
        );

        // then
        assertThat(response.getOfferChanceBonusRate()).isEqualTo(10);
        assertThat(response.isMeetFriendBonusApplied()).isTrue();
        assertThat(response.getOffers()).hasSize(5);
        assertThat(response.getOffers().get(0).getOfferId()).isEqualTo("OFFER-001");
        assertThat(response.getOffers().get(0).getJobType()).isEqualTo(JobType.SMALL_BIZ);
        assertThat(response.getOffers().get(4).getJobType()).isEqualTo(JobType.FREELANCER);
    }

    @DisplayName("이직 수락은 세션 가드 후 커리어 상태를 갱신하고 공개 응답을 반환한다.")
    @Test
    void transfer() {
        // given
        final User user = saveUser("career-transfer-user@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), 15, SessionStatus.IN_PROGRESS, CyclePhase.RECOVERY)
        );
        gameCareerRepository.saveAndFlush(
            createCareer(gameSession.getGameSessionId(), JobType.SMALL_BIZ, 30_000_000, 0, 2)
        );
        gameStatRepository.saveAndFlush(createStat(gameSession.getGameSessionId(), 70, 100));

        // when
        final JobTransferResponse response = gameCareerService.transfer(
            user.getId(),
            gameSession.getGameSessionId(),
            AcceptJobTransferServiceRequest.of("OFFER-004")
        );

        // then
        assertThat(response.getPreviousJobType()).isEqualTo(JobType.SMALL_BIZ);
        assertThat(response.getNewJobType()).isEqualTo(JobType.LARGE_BIZ);
        assertThat(response.getNewJobTitle()).isEqualTo("수습/인턴");
        assertThat(response.getNewSalary()).isEqualTo(45_000_000);
        assertThat(response.getProbationEndTurn()).isEqualTo(17);
        assertThat(response.isTenureReset()).isTrue();

        final GameCareer updatedCareer = gameCareerRepository.findById(
            gameSession.getGameSessionId().intValue()
        ).orElseThrow();
        assertThat(updatedCareer.getJobType()).isEqualTo(JobType.LARGE_BIZ);
        assertThat(updatedCareer.getJobTitle()).isEqualTo("수습/인턴");
        assertThat(updatedCareer.getSalary()).isEqualTo(45_000_000);
        assertThat(updatedCareer.getTenureTurns()).isZero();
        assertThat(updatedCareer.getProbationEndTurn()).isEqualTo(17);
    }

    @DisplayName("다른 사용자의 세션에 대한 커리어 조회는 GAME_SESSION_FORBIDDEN이 발생한다.")
    @Test
    void getJobOffersWithOtherUsersSession() {
        // given
        final User requester = saveUser("career-requester@example.com");
        final User owner = saveUser("career-owner@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(owner.getId(), 13, SessionStatus.IN_PROGRESS, CyclePhase.RECOVERY)
        );

        // when & then
        assertThatThrownBy(() -> gameCareerService.getJobOffers(
            requester.getId(),
            gameSession.getGameSessionId()
        ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GAME_SESSION_FORBIDDEN);
    }

    @DisplayName("종료된 세션의 이직 오퍼 조회는 GAME_SESSION_CLOSED가 발생한다.")
    @Test
    void getJobOffersWithClosedSession() {
        // given
        final User user = saveUser("career-closed@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), 13, SessionStatus.CLEAR, CyclePhase.RECOVERY)
        );

        // when & then
        assertThatThrownBy(() -> gameCareerService.getJobOffers(
            user.getId(),
            gameSession.getGameSessionId()
        ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GAME_SESSION_CLOSED);
    }

    @DisplayName("커리어 provider 상태가 비어 있으면 GLOBAL_EXTERNAL_RESPONSE_INVALID로 매핑한다.")
    @Test
    void negotiateSalaryWithoutCareerState() {
        // given
        final User user = saveUser("career-provider-missing@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), 13, SessionStatus.IN_PROGRESS, CyclePhase.BOOM)
        );

        // when & then
        assertThatThrownBy(() -> gameCareerService.negotiateSalary(
            user.getId(),
            gameSession.getGameSessionId()
        ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID);
    }

    @DisplayName("존재하지 않는 오퍼로 이직 수락을 요청하면 INVALID_INPUT_VALUE로 매핑한다.")
    @Test
    void transferWithUnknownOfferId() {
        // given
        final User user = saveUser("career-invalid-offer@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), 15, SessionStatus.IN_PROGRESS, CyclePhase.RECOVERY)
        );
        gameCareerRepository.saveAndFlush(
            createCareer(gameSession.getGameSessionId(), JobType.SMALL_BIZ, 30_000_000, 0, 1)
        );
        gameStatRepository.saveAndFlush(createStat(gameSession.getGameSessionId(), 70, 100));

        // when & then
        assertThatThrownBy(() -> gameCareerService.transfer(
            user.getId(),
            gameSession.getGameSessionId(),
            AcceptJobTransferServiceRequest.of("OFFER-999")
        ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    private User saveUser(final String email) {
        return userRepository.save(User.register(Email.of(email), "tester", "hashed-password"));
    }

    private GameSession createGameSession(
        final Long userId,
        final Integer currentTurn,
        final SessionStatus sessionStatus,
        final CyclePhase cyclePhase
    ) {
        final GameSession gameSession = GameSession.create(
            userId,
            1,
            "윤서",
            CharacterType.FEMALE,
            JobType.SMALL_BIZ,
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
            currentTurn,
            LocalDate.of(2026, 1, 1),
            Money.of(2_000_000L),
            Money.of(2_000_000L),
            cyclePhase
        );
        if (sessionStatus != SessionStatus.IN_PROGRESS) {
            gameSession.markEnding(sessionStatus);
        }
        return gameSession;
    }

    private GameCareer createCareer(
        final Long gameSessionId,
        final JobType jobType,
        final int salary,
        final int lastNegotiatedTurn,
        final int recentNetworkingCount
    ) {
        return GameCareer.builder()
            .gameId(gameSessionId.intValue())
            .jobType(jobType)
            .jobTitle("사원")
            .salary(salary)
            .tenureTurns(14)
            .recentStudyCount(1)
            .recentNetworkingCount(recentNetworkingCount)
            .negotiationPreparationScore(0)
            .lastNegotiatedTurn(lastNegotiatedTurn)
            .employmentStatus(EmploymentStatus.EMPLOYED)
            .probationEndTurn(null)
            .rehireAvailableTurn(null)
            .remainingUnemploymentBenefitTurns(0)
            .salaryBeforeResignation(null)
            .build();
    }

    private GameStat createStat(
        final Long gameSessionId,
        final int health,
        final int knowledge
    ) {
        return GameStat.builder()
            .gameId(gameSessionId.intValue())
            .health(health)
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
