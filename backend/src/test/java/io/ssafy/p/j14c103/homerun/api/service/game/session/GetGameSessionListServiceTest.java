package io.ssafy.p.j14c103.homerun.api.service.game.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import io.ssafy.p.j14c103.homerun.api.service.game.session.response.GameSessionListResponse;
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
class GetGameSessionListServiceTest {

    @Autowired
    private GetGameSessionListService getGameSessionListService;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        gameSessionRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @DisplayName("세션 목록 조회는 빈 슬롯을 포함한 3개 저장 슬롯을 반환한다.")
    @Test
    void getSessions() {
        // given
        final User user = saveUser("list-user@example.com");
        gameSessionRepository.saveAndFlush(createGameSession(user.getId(), 3, "민지", JobType.MID_BIZ, 303L));
        final GameSession first = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), 1, "윤서", JobType.STARTUP, 101L)
        );
        first.initializeCapital(
            Money.of(13_000_000L),
            Money.of(15_000_000L),
            LocalDate.of(2026, 1, 1),
            CyclePhase.RECOVERY
        );
        gameSessionRepository.saveAndFlush(first);

        // when
        final GameSessionListResponse response = getGameSessionListService.getSessions(user.getId());

        // then
        assertThat(response.getSessions())
            .extracting(
                GameSessionListResponse.SessionSummaryResponse::getSlotNumber,
                GameSessionListResponse.SessionSummaryResponse::getStatus,
                GameSessionListResponse.SessionSummaryResponse::getCharacterName
            )
            .containsExactly(
                tuple(1, "IN_PROGRESS", "윤서"),
                tuple(2, "EMPTY", null),
                tuple(3, "IN_PROGRESS", "민지")
            );
        assertThat(response.getSessions().get(0).getTotalAssets()).isEqualTo(15_000_000L);
        assertThat(response.getSessions().get(1).getSessionId()).isNull();
    }

    @DisplayName("존재하지 않는 사용자로 세션 목록을 조회하면 USER_NOT_FOUND가 발생한다.")
    @Test
    void getSessionsWithUnknownUser() {
        // when & then
        assertThatThrownBy(() -> getGameSessionListService.getSessions(99L))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    private User saveUser(final String email) {
        return userRepository.save(User.register(Email.of(email), "tester", "hashed-password"));
    }

    private GameSession createGameSession(
        final Long userId,
        final Integer slotNumber,
        final String characterName,
        final JobType jobType,
        final Long targetPropertyId
    ) {
        return GameSession.create(
            userId,
            slotNumber,
            characterName,
            CharacterType.FEMALE,
            jobType,
            HousingType.STUDIO,
            "11",
            "11680",
            targetPropertyId,
            DataSourceType.PROFILE
        );
    }
}
