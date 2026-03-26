package io.ssafy.p.j14c103.homerun.api.service.game.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.game.session.response.GameSessionDetailResponse;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class GetGameSessionDetailServiceTest {

    @Autowired
    private GetGameSessionDetailService getGameSessionDetailService;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        gameSessionRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @DisplayName("세션 상세 조회는 세션 스냅샷을 반환한다.")
    @Test
    void getSessionDetail() {
        // given
        final User user = saveUser("detail-user@example.com");
        final GameSession gameSession = GameSession.create(
            user.getId(),
            2,
            "세준",
            CharacterType.MALE,
            JobType.SMALL_BIZ,
            HousingType.STUDIO,
            "11",
            "11680",
            404L,
            DataSourceType.MY_DATA
        );
        gameSession.initializeCapital(
            Money.of(13_000_000L),
            Money.of(14_500_000L),
            LocalDate.of(2026, 1, 1),
            CyclePhase.RECOVERY
        );
        gameSession.advanceTurn(
            1,
            LocalDate.of(2026, 2, 1),
            Money.of(15_000_000L),
            Money.of(16_000_000L),
            CyclePhase.BOOM
        );
        final GameSession saved = gameSessionRepository.saveAndFlush(gameSession);

        // when
        final GameSessionDetailResponse response =
            getGameSessionDetailService.getSessionDetail(user.getId(), saved.getGameSessionId());

        // then
        assertThat(response.getSessionId()).isEqualTo(saved.getGameSessionId());
        assertThat(response.getSlotNumber()).isEqualTo(2);
        assertThat(response.getCharacterName()).isEqualTo("세준");
        assertThat(response.getCurrentTurn()).isEqualTo(1);
        assertThat(response.getCurrentDate()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(response.getCyclePhase()).isEqualTo(CyclePhase.BOOM);
        assertThat(response.getCashBalance()).isEqualTo(15_000_000L);
        assertThat(response.getNetWorth()).isEqualTo(16_000_000L);
    }

    @DisplayName("다른 사용자의 세션 상세 조회는 GAME_SESSION_FORBIDDEN이 발생한다.")
    @Test
    void getOtherUsersSessionDetail() {
        // given
        final User requester = saveUser("detail-requester@example.com");
        final User owner = saveUser("detail-owner@example.com");
        final GameSession saved = gameSessionRepository.saveAndFlush(GameSession.create(
            owner.getId(),
            1,
            "타인세션",
            CharacterType.MALE,
            JobType.LARGE_BIZ,
            HousingType.OWNED_APT,
            "11",
            "11710",
            202L,
            DataSourceType.PROFILE
        ));

        // when & then
        assertThatThrownBy(() ->
            getGameSessionDetailService.getSessionDetail(requester.getId(), saved.getGameSessionId())
        )
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GAME_SESSION_FORBIDDEN);
    }

    @DisplayName("존재하지 않는 세션 상세 조회는 GAME_SESSION_NOT_FOUND가 발생한다.")
    @Test
    void getUnknownSessionDetail() {
        // given
        final User requester = saveUser("detail-not-found@example.com");

        // when & then
        assertThatThrownBy(() -> getGameSessionDetailService.getSessionDetail(requester.getId(), 999L))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GAME_SESSION_NOT_FOUND);
    }

    private User saveUser(final String email) {
        return userRepository.save(User.register(Email.of(email), "tester", "hashed-password"));
    }
}
