package io.ssafy.p.j14c103.homerun.api.service.game.realestate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.ssafy.p.j14c103.homerun.api.service.game.realestate.request.SubmitContractReviewServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.game.realestate.response.ContractReviewResponse;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.WorldContractReviewSubmitService;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.WorldPurchaseValidationService;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.response.PurchaseValidationFailureCode;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.response.WorldContractReviewSubmitServiceResponse;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.response.WorldPurchaseValidationServiceResponse;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
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
import io.ssafy.p.j14c103.homerun.domain.world.housing.ContractResult;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import io.ssafy.p.j14c103.homerun.support.IntegrationTestSupport;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class RealEstateContractReviewServiceTest extends IntegrationTestSupport {

    @Autowired
    private RealEstateContractReviewService realEstateContractReviewService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @MockitoBean
    private WorldContractReviewSubmitService worldContractReviewSubmitService;

    @MockitoBean
    private WorldPurchaseValidationService worldPurchaseValidationService;

    @DisplayName("WARNING 결과여도 구매 검증이 통과하면 success true와 PARTIAL 응답을 반환한다")
    @Test
    void reviewSuccess() {
        // given
        final User user = saveUser("real-estate-contract-success@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(user.getId(), 101L));
        given(worldContractReviewSubmitService.submit(any()))
            .willReturn(WorldContractReviewSubmitServiceResponse.of(
                2,
                2,
                ContractResult.WARNING,
                "주의가 필요한 항목이 있습니다."
            ));
        given(worldPurchaseValidationService.validate(any()))
            .willReturn(WorldPurchaseValidationServiceResponse.success(101L, 375_000_000L, HousingType.OWNED_APT));

        // when
        final ContractReviewResponse response = realEstateContractReviewService.review(
            user.getId(),
            gameSession.getGameSessionId(),
            101L,
            SubmitContractReviewServiceRequest.of(List.of("TRAP-01", "TRAP-02"))
        );

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getContractResult()).isEqualTo("PARTIAL");
        assertThat(response.getMessage()).isEqualTo("주의가 필요한 항목이 있습니다.");
        then(worldContractReviewSubmitService).should().submit(any());
        then(worldPurchaseValidationService).should().validate(any());
    }

    @DisplayName("계약 검토 결과가 FAIL이면 구매 검증 연계 후 success false와 TRAPPED 응답을 반환한다")
    @Test
    void reviewFailure() {
        // given
        final User user = saveUser("real-estate-contract-failure@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(user.getId(), 101L));
        given(worldContractReviewSubmitService.submit(any()))
            .willReturn(WorldContractReviewSubmitServiceResponse.of(
                2,
                1,
                ContractResult.FAIL,
                "위험한 계약입니다."
            ));
        given(worldPurchaseValidationService.validate(any()))
            .willReturn(WorldPurchaseValidationServiceResponse.failure(
                PurchaseValidationFailureCode.CONTRACT_REVIEW_FAILED
            ));

        // when
        final ContractReviewResponse response = realEstateContractReviewService.review(
            user.getId(),
            gameSession.getGameSessionId(),
            101L,
            SubmitContractReviewServiceRequest.of(List.of("TRAP-02"))
        );

        // then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getContractResult()).isEqualTo("TRAPPED");
        assertThat(response.getTrapsDetected()).isEqualTo(2);
        assertThat(response.getTrapsCorrectlyIdentified()).isEqualTo(1);
        assertThat(response.getMessage()).isEqualTo("위험한 계약입니다.");
    }

    @DisplayName("이미 다른 자가 실매물을 보유 중이면 success false와 충돌 메시지를 반환한다")
    @Test
    void reviewWithActualPropertyConflict() {
        // given
        final User user = saveUser("real-estate-contract-conflict@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(user.getId(), 101L));
        given(worldContractReviewSubmitService.submit(any()))
            .willReturn(WorldContractReviewSubmitServiceResponse.of(
                1,
                1,
                ContractResult.SAFE,
                "서류 검토를 통과했습니다."
            ));
        given(worldPurchaseValidationService.validate(any()))
            .willReturn(WorldPurchaseValidationServiceResponse.failure(
                PurchaseValidationFailureCode.ACTUAL_PROPERTY_CONFLICT
            ));

        // when
        final ContractReviewResponse response = realEstateContractReviewService.review(
            user.getId(),
            gameSession.getGameSessionId(),
            101L,
            SubmitContractReviewServiceRequest.of(List.of("TRAP-01"))
        );

        // then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getContractResult()).isEqualTo("SAFE");
        assertThat(response.getMessage()).isEqualTo("이미 다른 자가 실매물을 보유 중입니다.");
    }

    @DisplayName("세션 목표 매물과 다른 요청이면 HOUSING_CONTRACT_REVIEW_TARGET_MISMATCH가 발생한다")
    @Test
    void reviewWithTargetMismatch() {
        // given
        final User user = saveUser("real-estate-contract-target@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(user.getId(), 101L));
        given(worldContractReviewSubmitService.submit(any()))
            .willThrow(new HomerunException(ErrorCode.HOUSING_CONTRACT_REVIEW_TARGET_MISMATCH));

        // when & then
        assertThatThrownBy(() -> realEstateContractReviewService.review(
            user.getId(),
            gameSession.getGameSessionId(),
            202L,
            SubmitContractReviewServiceRequest.of(List.of("TRAP-01"))
        ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.HOUSING_CONTRACT_REVIEW_TARGET_MISMATCH);
        then(worldPurchaseValidationService).shouldHaveNoInteractions();
    }

    @DisplayName("다른 사용자의 세션이면 GAME_SESSION_FORBIDDEN이 발생한다")
    @Test
    void reviewWithForbiddenSession() {
        // given
        final User owner = saveUser("real-estate-contract-owner@example.com");
        final User requester = saveUser("real-estate-contract-requester@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(owner.getId(), 101L));

        // when & then
        assertThatThrownBy(() -> realEstateContractReviewService.review(
            requester.getId(),
            gameSession.getGameSessionId(),
            101L,
            SubmitContractReviewServiceRequest.of(List.of("TRAP-01"))
        ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GAME_SESSION_FORBIDDEN);
        then(worldContractReviewSubmitService).shouldHaveNoInteractions();
        then(worldPurchaseValidationService).shouldHaveNoInteractions();
    }

    @DisplayName("존재하지 않는 세션이면 GAME_SESSION_NOT_FOUND가 발생한다")
    @Test
    void reviewWithUnknownSession() {
        // given
        final User user = saveUser("real-estate-contract-unknown-session@example.com");

        // when & then
        assertThatThrownBy(() -> realEstateContractReviewService.review(
            user.getId(),
            9999L,
            101L,
            SubmitContractReviewServiceRequest.of(List.of("TRAP-01"))
        ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GAME_SESSION_NOT_FOUND);
        then(worldContractReviewSubmitService).shouldHaveNoInteractions();
        then(worldPurchaseValidationService).shouldHaveNoInteractions();
    }

    @DisplayName("종료된 세션이면 world 계약 검토를 호출하지 않고 GAME_SESSION_CLOSED가 발생한다")
    @Test
    void reviewWithClosedSession() {
        // given
        final User user = saveUser("real-estate-contract-closed@example.com");
        final GameSession gameSession = createGameSession(user.getId(), 101L);
        gameSession.markEnding(SessionStatus.CLEAR);
        gameSessionRepository.saveAndFlush(gameSession);

        // when & then
        assertThatThrownBy(() -> realEstateContractReviewService.review(
            user.getId(),
            gameSession.getGameSessionId(),
            101L,
            SubmitContractReviewServiceRequest.of(List.of("TRAP-01"))
        ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GAME_SESSION_CLOSED);
        then(worldContractReviewSubmitService).shouldHaveNoInteractions();
        then(worldPurchaseValidationService).shouldHaveNoInteractions();
    }

    private User saveUser(final String email) {
        return userRepository.save(User.register(Email.of(email), "tester", "hashed-password"));
    }

    private GameSession createGameSession(final Long userId, final Long targetPropertyId) {
        final GameSession gameSession = GameSession.create(
            userId,
            1,
            "윤서",
            CharacterType.FEMALE,
            JobType.STARTUP,
            HousingType.STUDIO,
            "11",
            "11680",
            targetPropertyId,
            DataSourceType.PROFILE
        );
        gameSession.initializeCapital(
            Money.of(20_000_000L),
            Money.of(20_000_000L),
            LocalDate.of(2026, 1, 1),
            CyclePhase.BOOM
        );
        return gameSession;
    }
}
