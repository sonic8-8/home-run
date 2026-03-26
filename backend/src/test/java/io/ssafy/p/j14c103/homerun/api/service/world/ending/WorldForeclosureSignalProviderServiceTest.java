package io.ssafy.p.j14c103.homerun.api.service.world.ending;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.world.ending.request.WorldForeclosureSignalProviderRequest;
import io.ssafy.p.j14c103.homerun.api.service.world.ending.response.WorldForeclosureSignalProviderResponse;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WorldForeclosureSignalProviderServiceTest {

    @Autowired
    private WorldForeclosureSignalProviderService worldForeclosureSignalProviderService;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @DisplayName("장기 연체와 강제 매각, 주거 상실이 모두 있으면 foreclosure signal이 생성된다.")
    @Test
    void getForeclosureSignal() {
        // given
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession());

        // when
        final WorldForeclosureSignalProviderResponse response =
            worldForeclosureSignalProviderService.getForeclosureSignal(
                WorldForeclosureSignalProviderRequest.of(
                    gameSession.getGameSessionId(),
                    3,
                    true,
                    true
                )
            );

        // then
        assertThat(response.getSessionId()).isEqualTo(gameSession.getGameSessionId());
        assertThat(response.isSignalGenerated()).isTrue();
        assertThat(response.getLongOverdueTurns()).isEqualTo(3);
        assertThat(response.isForcedSaleOccurred()).isTrue();
        assertThat(response.isHousingLossSignal()).isTrue();
    }

    @DisplayName("셋 중 하나라도 빠지면 foreclosure signal이 생성되지 않는다.")
    @Test
    void getForeclosureSignalWithoutAllConditions() {
        // given
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession());

        // when
        final WorldForeclosureSignalProviderResponse onlyHousingLoss =
            worldForeclosureSignalProviderService.getForeclosureSignal(
                WorldForeclosureSignalProviderRequest.of(
                    gameSession.getGameSessionId(),
                    0,
                    false,
                    true
                )
            );
        final WorldForeclosureSignalProviderResponse onlyLongOverdue =
            worldForeclosureSignalProviderService.getForeclosureSignal(
                WorldForeclosureSignalProviderRequest.of(
                    gameSession.getGameSessionId(),
                    3,
                    false,
                    false
                )
            );

        // then
        assertThat(onlyHousingLoss.isSignalGenerated()).isFalse();
        assertThat(onlyLongOverdue.isSignalGenerated()).isFalse();
    }

    @DisplayName("계약 실패만 있고 강제 매각과 주거 상실이 없으면 foreclosure signal이 생성되지 않는다.")
    @Test
    void getForeclosureSignalWithoutForcedSaleAndHousingLoss() {
        // given
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession());

        // when
        final WorldForeclosureSignalProviderResponse response =
            worldForeclosureSignalProviderService.getForeclosureSignal(
                WorldForeclosureSignalProviderRequest.of(
                    gameSession.getGameSessionId(),
                    3,
                    false,
                    false
                )
            );

        // then
        assertThat(response.isSignalGenerated()).isFalse();
    }

    @DisplayName("존재하지 않는 세션이면 WORLD_SESSION_NOT_FOUND 예외가 발생한다.")
    @Test
    void getForeclosureSignalWithUnknownSession() {
        // when & then
        assertThatThrownBy(() -> worldForeclosureSignalProviderService.getForeclosureSignal(
            WorldForeclosureSignalProviderRequest.of(
                9999L,
                3,
                true,
                true
            )
        ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.WORLD_SESSION_NOT_FOUND);
    }

    private GameSession createGameSession() {
        return GameSession.create(
            1L,
            1,
            "홍길동",
            CharacterType.MALE,
            JobType.SMALL_BIZ,
            HousingType.NONE,
            "11",
            "11680",
            101L,
            DataSourceType.MY_DATA
        );
    }
}
