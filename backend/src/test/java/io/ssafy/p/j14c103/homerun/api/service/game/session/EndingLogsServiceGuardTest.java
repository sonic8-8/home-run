package io.ssafy.p.j14c103.homerun.api.service.game.session;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.ssafy.p.j14c103.homerun.api.service.user.UserAuthContext;
import io.ssafy.p.j14c103.homerun.api.service.user.UserAuthContextService;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.report.GameReportRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.report.GameTimelineRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EndingLogsServiceGuardTest {

    private EndingLogsService endingLogsService;

    @Mock
    private GameSessionRepository gameSessionRepository;

    @Mock
    private GameTimelineRepository gameTimelineRepository;

    @Mock
    private GameReportRepository gameReportRepository;

    @Mock
    private UserAuthContextService userAuthContextService;

    @BeforeEach
    void setUp() {
        endingLogsService = new EndingLogsService(
            gameSessionRepository,
            gameTimelineRepository,
            gameReportRepository,
            userAuthContextService
        );
    }

    @DisplayName("진행 중인 세션이면 엔딩 로그 조회가 ENDING_REPORT_NOT_READY로 막힌다")
    @Test
    void getLogsWhenSessionIsInProgress() {
        // given
        final Long userId = 1L;
        final Long sessionId = 10L;
        final GameSession gameSession = createInProgressSession(userId, 450L);
        given(userAuthContextService.getContext(userId))
            .willReturn(new UserAuthContext(userId, null));
        given(gameSessionRepository.findById(sessionId))
            .willReturn(Optional.of(gameSession));

        // when // then
        assertThatThrownBy(() -> endingLogsService.getLogs(userId, sessionId))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.ENDING_REPORT_NOT_READY);
        verify(gameReportRepository, never()).existsById(anyLong());
        verify(gameTimelineRepository, never()).findAllByGameSessionIdOrderByTurnNumberAsc(anyLong());
        verify(userAuthContextService).getContext(userId);
        verify(gameSessionRepository).findById(sessionId);
    }

    private GameSession createInProgressSession(final Long userId, final Long targetPropertyId) {
        return GameSession.create(
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
    }
}
