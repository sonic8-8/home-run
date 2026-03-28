package io.ssafy.p.j14c103.homerun.domain.gamesession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GameSessionTest {

    @DisplayName("세션 소유자가 일치하면 접근 검증을 통과한다.")
    @Test
    void assertOwner() {
        // given
        final GameSession gameSession = createGameSession();

        // when & then
        assertThatCode(() -> gameSession.assertOwner(1L))
            .doesNotThrowAnyException();
    }

    @DisplayName("세션 소유자가 아니면 GAME_SESSION_FORBIDDEN 예외가 발생한다.")
    @Test
    void assertOwner_forbidden() {
        // given
        final GameSession gameSession = createGameSession();

        // when & then
        assertThatThrownBy(() -> gameSession.assertOwner(2L))
            .isInstanceOf(HomerunException.class)
            .extracting(throwable -> ((HomerunException) throwable).getErrorCode())
            .isEqualTo(ErrorCode.GAME_SESSION_FORBIDDEN);
    }

    @DisplayName("진행 중인 세션은 진행 중 상태 검증을 통과한다.")
    @Test
    void assertInProgress() {
        // given
        final GameSession gameSession = createGameSession();

        // when & then
        assertThatCode(gameSession::assertInProgress)
            .doesNotThrowAnyException();
    }

    @DisplayName("종료된 세션은 진행 중 상태 검증을 통과할 수 없다.")
    @Test
    void assertInProgress_closedSession() {
        // given
        final GameSession gameSession = createGameSession();
        gameSession.markEnding(SessionStatus.CLEAR);

        // when & then
        assertThatThrownBy(gameSession::assertInProgress)
            .isInstanceOf(HomerunException.class)
            .extracting(throwable -> ((HomerunException) throwable).getErrorCode())
            .isEqualTo(ErrorCode.GAME_SESSION_CLOSED);
    }

    @DisplayName("종료 상태로 전환하면 세션 상태가 종료 값으로 바뀐다.")
    @Test
    void markEnding() {
        // given
        final GameSession gameSession = createGameSession();

        // when
        gameSession.markEnding(SessionStatus.CLEAR);

        // then
        assertThat(gameSession.getSessionStatus()).isEqualTo(SessionStatus.CLEAR);
    }

    @DisplayName("초기 자본 설정은 시작 자산과 날짜, 사이클을 반영한다.")
    @Test
    void initializeCapital() {
        // given
        final GameSession gameSession = createGameSession();

        // when
        gameSession.initializeCapital(
            Money.of(13_000_000L),
            Money.of(15_000_000L),
            Money.of(13_000_000L),
            LocalDate.of(2026, 1, 1),
            CyclePhase.RECOVERY
        );

        // then
        assertThat(gameSession.getCashBalance()).isEqualTo(Money.of(13_000_000L));
        assertThat(gameSession.getTotalAssets()).isEqualTo(Money.of(15_000_000L));
        assertThat(gameSession.getNetWorth()).isEqualTo(Money.of(13_000_000L));
        assertThat(gameSession.getCurrentDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(gameSession.getCyclePhase()).isEqualTo(CyclePhase.RECOVERY);
    }

    private static GameSession createGameSession() {
        return GameSession.create(
            1L,
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
