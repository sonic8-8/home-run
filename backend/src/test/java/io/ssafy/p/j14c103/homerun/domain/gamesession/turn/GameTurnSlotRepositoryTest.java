package io.ssafy.p.j14c103.homerun.domain.gamesession.turn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.support.IntegrationTestSupport;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class GameTurnSlotRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private GameSessionTurnSlotRepository gameTurnSlotRepository;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private EntityManager entityManager;

    @DisplayName("game_turn_slots는 세션과 턴 기준으로 슬롯 순서대로 조회된다")
    @Test
    void findAllByGameSessionIdAndTurnNumberOrderBySlotIndex() {
        // given
        final GameSession gameSession = saveGameSession(11L, 1);
        gameTurnSlotRepository.saveAndFlush(GameTurnSlot.create(
            gameSession.getGameSessionId(),
            3,
            2,
            ActionType.SIDE_JOB,
            ActionCategory.ACTIVITY,
            false
        ));
        gameTurnSlotRepository.saveAndFlush(GameTurnSlot.create(
            gameSession.getGameSessionId(),
            3,
            0,
            ActionType.STUDY,
            ActionCategory.ACTIVITY,
            false
        ));
        gameTurnSlotRepository.saveAndFlush(GameTurnSlot.create(
            gameSession.getGameSessionId(),
            3,
            1,
            ActionType.REST,
            ActionCategory.ACTIVITY,
            true
        ));
        entityManager.clear();

        // when
        final List<GameTurnSlot> found = gameTurnSlotRepository
            .findAllByGameSessionIdAndTurnNumberOrderBySlotIndex(gameSession.getGameSessionId(), 3);

        // then
        assertThat(found)
            .extracting(GameTurnSlot::getSlotIndex, GameTurnSlot::getActionType, GameTurnSlot::isForcedAction)
            .containsExactly(
                tuple(0, ActionType.STUDY, false),
                tuple(1, ActionType.REST, true),
                tuple(2, ActionType.SIDE_JOB, false)
            );
    }

    @DisplayName("같은 세션의 같은 턴 슬롯 인덱스는 중복 저장할 수 없다")
    @Test
    void saveDuplicateTurnSlot() {
        // given
        final GameSession gameSession = saveGameSession(12L, 2);
        gameTurnSlotRepository.saveAndFlush(GameTurnSlot.create(
            gameSession.getGameSessionId(),
            1,
            0,
            ActionType.STUDY,
            ActionCategory.ACTIVITY,
            false
        ));

        final GameTurnSlot duplicate = GameTurnSlot.create(
            gameSession.getGameSessionId(),
            1,
            0,
            ActionType.REST,
            ActionCategory.ACTIVITY,
            false
        );

        // when & then
        assertThatThrownBy(() -> gameTurnSlotRepository.saveAndFlush(duplicate))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @DisplayName("같은 세션의 같은 턴에 확정 슬롯이 있으면 중복 커밋 방어 조회가 가능하다")
    @Test
    void existsByGameSessionIdAndTurnNumber() {
        // given
        final GameSession gameSession = saveGameSession(13L, 1);
        gameTurnSlotRepository.saveAndFlush(GameTurnSlot.create(
            gameSession.getGameSessionId(),
            4,
            0,
            ActionType.STUDY,
            ActionCategory.ACTIVITY,
            false
        ));

        // when
        final boolean exists = gameTurnSlotRepository.existsByGameSessionIdAndTurnNumber(
            gameSession.getGameSessionId(),
            4
        );
        final boolean notExists = gameTurnSlotRepository.existsByGameSessionIdAndTurnNumber(
            gameSession.getGameSessionId(),
            5
        );

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    private GameSession saveGameSession(
        final Long userId,
        final Integer slotNumber
    ) {
        return gameSessionRepository.saveAndFlush(GameSession.create(
            userId,
            slotNumber,
            "턴 슬롯 세션",
            CharacterType.FEMALE,
            JobType.STARTUP,
            HousingType.STUDIO,
            "11",
            "11680",
            101L,
            DataSourceType.PROFILE
        ));
    }

}
