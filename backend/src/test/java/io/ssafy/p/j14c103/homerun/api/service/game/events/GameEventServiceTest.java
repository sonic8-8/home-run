package io.ssafy.p.j14c103.homerun.api.service.game.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.ssafy.p.j14c103.homerun.api.service.game.events.request.ResolveEventServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.game.events.response.PendingEventsResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.events.response.ResolveEventResponse;
import io.ssafy.p.j14c103.homerun.api.service.world.WorldEventResolveExecutionService;
import io.ssafy.p.j14c103.homerun.api.service.world.WorldPendingEventProviderService;
import io.ssafy.p.j14c103.homerun.api.service.world.response.PendingEventsProviderResponse;
import io.ssafy.p.j14c103.homerun.api.service.world.result.EventResolveExecutionResult;
import io.ssafy.p.j14c103.homerun.api.service.world.result.EventResolveResult;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventPresentationType;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class GameEventServiceTest {

    @Autowired
    private GameEventService gameEventService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @MockitoBean
    private WorldPendingEventProviderService worldPendingEventProviderService;

    @MockitoBean
    private WorldEventResolveExecutionService worldEventResolveExecutionService;

    @AfterEach
    void tearDown() {
        gameSessionRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @DisplayName("본인 세션의 pending 이벤트 조회는 world 응답을 facade 계약으로 변환한다")
    @Test
    void getPendingEvents() {
        final User user = saveUser("pending-user@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(user.getId()));

        given(worldPendingEventProviderService.getPendingEvents(gameSession.getGameSessionId()))
            .willReturn(PendingEventsProviderResponse.of(List.of(
                PendingEventsProviderResponse.PendingEventItem.of(
                    301,
                    EventPresentationType.JOB_TRANSFER,
                    "이직 제안",
                    "더 좋은 조건으로 이직 제안이 도착했습니다.",
                    "/images/events/job-transfer.png",
                    List.of(
                        PendingEventsProviderResponse.PendingEventChoiceItem.of(
                            701,
                            "A",
                            "수락",
                            "연봉을 올리고 이직합니다."
                        )
                    ),
                    "OO 기업 인사팀",
                    "김싸피 님",
                    LocalDate.of(2026, 1, 1),
                    36_000_000,
                    31_000_000
                )
            )));

        final PendingEventsResponse response = gameEventService.getPendingEvents(
            user.getId(),
            gameSession.getGameSessionId()
        );

        assertThat(response.getEvents()).hasSize(1);
        assertThat(response.getEvents().get(0).getEventId()).isEqualTo(301);
        assertThat(response.getEvents().get(0).getType()).isEqualTo(EventPresentationType.JOB_TRANSFER);
        assertThat(response.getEvents().get(0).getChoices()).hasSize(1);
        assertThat(response.getEvents().get(0).getChoices().get(0).getChoiceCode()).isEqualTo("A");
        then(worldPendingEventProviderService).should().getPendingEvents(gameSession.getGameSessionId());
    }

    @DisplayName("타인 세션의 pending 이벤트 조회는 GAME_SESSION_FORBIDDEN이 발생한다")
    @Test
    void getPendingEventsForbidden() {
        final User requester = saveUser("pending-requester@example.com");
        final User owner = saveUser("pending-owner@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(owner.getId()));

        assertThatThrownBy(() -> gameEventService.getPendingEvents(
                requester.getId(),
                gameSession.getGameSessionId()
            ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GAME_SESSION_FORBIDDEN);
        then(worldPendingEventProviderService).shouldHaveNoInteractions();
    }

    @DisplayName("본인 세션의 이벤트 resolve는 world executor 결과를 facade 계약으로 변환한다")
    @Test
    void resolveEvent() {
        final User user = saveUser("resolve-user@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(user.getId()));
        final ResolveEventServiceRequest request = ResolveEventServiceRequest.of(701);

        given(worldEventResolveExecutionService.resolveEvent(
            gameSession.getGameSessionId(),
            301,
            701
        )).willReturn(EventResolveExecutionResult.of(
            301,
            1201,
            701,
            "A",
            List.of(
                EventResolveResult.ResolvedEffect.of(
                    1,
                    "IMMEDIATE",
                    "game_career",
                    "salary",
                    "ADD",
                    5_000_000,
                    null,
                    null,
                    null,
                    null,
                    "연봉이 상승했습니다."
                )
            ),
            "이직 제안을 수락했다."
        ));

        final ResolveEventResponse response = gameEventService.resolveEvent(
            user.getId(),
            gameSession.getGameSessionId(),
            301,
            request
        );

        assertThat(response.getEventId()).isEqualTo(301);
        assertThat(response.getGameEventId()).isEqualTo(1201);
        assertThat(response.getChoiceId()).isEqualTo(701);
        assertThat(response.getSelectedChoiceCode()).isEqualTo("A");
        assertThat(response.getResultEffects()).hasSize(1);
        assertThat(response.getResultSummary()).isEqualTo("이직 제안을 수락했다.");
        then(worldEventResolveExecutionService).should()
            .resolveEvent(gameSession.getGameSessionId(), 301, 701);
    }

    @DisplayName("타인 세션의 이벤트 resolve는 GAME_SESSION_FORBIDDEN이 발생한다")
    @Test
    void resolveEventForbidden() {
        final User requester = saveUser("resolve-requester@example.com");
        final User owner = saveUser("resolve-owner@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(owner.getId()));

        assertThatThrownBy(() -> gameEventService.resolveEvent(
                requester.getId(),
                gameSession.getGameSessionId(),
                301,
                ResolveEventServiceRequest.of(701)
            ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GAME_SESSION_FORBIDDEN);
        then(worldEventResolveExecutionService).shouldHaveNoInteractions();
    }

    private User saveUser(final String email) {
        return userRepository.save(User.register(Email.of(email), "tester", "hashed-password"));
    }

    private GameSession createGameSession(final Long userId) {
        return GameSession.create(
            userId,
            1,
            "윤서",
            CharacterType.FEMALE,
            JobType.STARTUP,
            HousingType.STUDIO,
            "11",
            "11680",
            101L,
            DataSourceType.PROFILE
        );
    }
}
