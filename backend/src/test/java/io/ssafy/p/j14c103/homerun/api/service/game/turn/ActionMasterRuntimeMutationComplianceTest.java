package io.ssafy.p.j14c103.homerun.api.service.game.turn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import io.ssafy.p.j14c103.homerun.api.service.game.port.TurnPreviewCalculator;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.request.SubmitTurnSlotsServiceRequest;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.ActionType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.TurnDraftRepository;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.support.IntegrationTestSupport;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
class ActionMasterRuntimeMutationComplianceTest extends IntegrationTestSupport {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SubmitTurnSlotsService submitTurnSlotsService;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private TurnPreviewCalculator turnPreviewCalculator;

    @MockitoBean
    private TurnDraftRepository turnDraftRepository;

    @AfterEach
    void tearDown() {
        gameSessionRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Order(1)
    @DisplayName("애플리케이션 부팅만으로 action_masters 테이블을 생성하지 않는다.")
    @Test
    void bootDoesNotCreateActionMastersTable() {
        // when
        final boolean actionMastersTableExists = actionMastersTableExists();

        // then
        assertThat(actionMastersTableExists).isFalse();
    }

    @Order(2)
    @DisplayName("턴 슬롯 제출은 누락된 action_masters 테이블을 런타임에 생성하지 않는다.")
    @Test
    void submitTurnSlotsDoesNotCreateActionMastersTableAtRuntime() {
        // given
        final User user = saveUser("action-master-runtime-mutation@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(user.getId()));
        final SubmitTurnSlotsServiceRequest request = SubmitTurnSlotsServiceRequest.of(
            List.of(
                SubmitTurnSlotsServiceRequest.TurnSlotRequest.of(0, ActionType.REST),
                SubmitTurnSlotsServiceRequest.TurnSlotRequest.of(1, ActionType.REST),
                SubmitTurnSlotsServiceRequest.TurnSlotRequest.of(2, ActionType.REST)
            )
        );
        given(turnPreviewCalculator.calculate(any(GameSession.class), any(SubmitTurnSlotsServiceRequest.class)))
            .willReturn(TurnPreviewCalculator.TurnPreviewResult.of(
                List.of(
                    TurnPreviewCalculator.PreviewSlot.of(0, ActionType.REST, false),
                    TurnPreviewCalculator.PreviewSlot.of(1, ActionType.REST, false),
                    TurnPreviewCalculator.PreviewSlot.of(2, ActionType.REST, false)
                ),
                Money.zero(),
                Map.of(
                    "health", 0,
                    "fatigue", 0,
                    "stress", 0,
                    "happiness", 0,
                    "knowledge", 0
                )
            ));
        jdbcTemplate.execute("drop table if exists action_masters");
        assertThat(actionMastersTableExists()).isFalse();

        // when
        submitTurnSlotsService.submitTurnSlots(
            user.getId(),
            gameSession.getGameSessionId(),
            request
        );

        // then
        assertThat(actionMastersTableExists()).isFalse();
    }

    private boolean actionMastersTableExists() {
        final Integer count = jdbcTemplate.queryForObject(
            """
                select count(*)
                  from information_schema.tables
                 where lower(table_name) = lower(?)
                """,
            Integer.class,
            "action_masters"
        );
        return count != null && count > 0;
    }

    private User saveUser(final String email) {
        return userRepository.save(User.register(Email.of(email), "tester", "hashed-password"));
    }

    private GameSession createGameSession(final Long userId) {
        final GameSession gameSession = GameSession.create(
            userId,
            1,
            "윤서",
            CharacterType.FEMALE,
            io.ssafy.p.j14c103.homerun.domain.character.career.JobType.STARTUP,
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
            CyclePhase.BOOM
        );
        return gameSession;
    }
}
