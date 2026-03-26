package io.ssafy.p.j14c103.homerun.api.service.game.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.then;

import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class DeleteGameSessionServiceTest {

    @Autowired
    private DeleteGameSessionService deleteGameSessionService;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private GameSessionCleanupService gameSessionCleanupService;

    @AfterEach
    void tearDown() {
        gameSessionRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @DisplayName("세션 삭제는 cleanup 호출 후 세션을 제거한다.")
    @Test
    void delete() {
        // given
        final User user = saveUser("delete-user@example.com");
        final GameSession saved = gameSessionRepository.saveAndFlush(createGameSession(user.getId(), 1, 101L));

        // when
        deleteGameSessionService.delete(user.getId(), saved.getGameSessionId());

        // then
        assertThat(gameSessionRepository.findById(saved.getGameSessionId())).isEmpty();
        then(gameSessionCleanupService).should().deleteAllByGameSessionId(saved.getGameSessionId());
    }

    @DisplayName("다른 사용자의 세션 삭제는 GAME_SESSION_FORBIDDEN이 발생한다.")
    @Test
    void deleteOtherUsersSession() {
        // given
        final User requester = saveUser("delete-requester@example.com");
        final User owner = saveUser("delete-owner@example.com");
        final GameSession saved = gameSessionRepository.saveAndFlush(createGameSession(owner.getId(), 1, 101L));

        // when & then
        assertThatThrownBy(() ->
            deleteGameSessionService.delete(requester.getId(), saved.getGameSessionId())
        )
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GAME_SESSION_FORBIDDEN);
        then(gameSessionCleanupService).shouldHaveNoInteractions();
    }

    @DisplayName("존재하지 않는 세션 삭제는 GAME_SESSION_NOT_FOUND가 발생한다.")
    @Test
    void deleteUnknownSession() {
        // given
        final User requester = saveUser("delete-not-found@example.com");

        // when & then
        assertThatThrownBy(() -> deleteGameSessionService.delete(requester.getId(), 999L))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GAME_SESSION_NOT_FOUND);
        then(gameSessionCleanupService).shouldHaveNoInteractions();
    }

    private User saveUser(final String email) {
        return userRepository.save(User.register(Email.of(email), "tester", "hashed-password"));
    }

    private GameSession createGameSession(final Long userId, final Integer slotNumber, final Long targetPropertyId) {
        return GameSession.create(
            userId,
            slotNumber,
            "윤서",
            CharacterType.FEMALE,
            JobType.STARTUP,
            HousingType.STUDIO,
            "11",
            "11680",
            targetPropertyId,
            DataSourceType.PROFILE
        );
    }
}
