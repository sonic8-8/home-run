package io.ssafy.p.j14c103.homerun.domain.gamesession.turn;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.support.RedisContainerTestSupport;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

@Tag("container")
@Testcontainers(disabledWithoutDocker = true)
class TurnDraftRepositoryTest extends RedisContainerTestSupport {

    @Autowired
    private TurnDraftRepository turnDraftRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @AfterEach
    void tearDown() {
        final Set<String> keys = stringRedisTemplate.keys("session:*:turn");

        if (keys == null || keys.isEmpty()) {
            return;
        }

        stringRedisTemplate.delete(keys);
    }

    @DisplayName("TurnDraft를 Redis에 저장하고 다시 조회할 수 있다.")
    @Test
    void saveAndFindBySessionId() {
        // given
        final TurnDraft turnDraft = createTurnDraft(
            101L,
            12,
            List.of(
                TurnDraftSlot.of(0, ActionType.STUDY, false),
                TurnDraftSlot.of(1, ActionType.NETWORKING, false),
                TurnDraftSlot.of(2, ActionType.REST, true)
            ),
            Money.of(150_000L),
            Money.of(150_000L),
            Money.of(300_000L),
            Map.of(
                "health", 5,
                "knowledge", 8
            )
        );

        // when
        turnDraftRepository.save(turnDraft);
        final TurnDraft found = turnDraftRepository.findBySessionId(101L)
            .orElseThrow();

        // then
        assertThat(found.getSessionId()).isEqualTo(101L);
        assertThat(found.getTurnNumber()).isEqualTo(12);
        assertThat(found.getSlots())
            .extracting(TurnDraftSlot::getSlotIndex, TurnDraftSlot::getActionType, TurnDraftSlot::isForcedAction)
            .containsExactly(
                org.assertj.core.groups.Tuple.tuple(0, ActionType.STUDY, false),
                org.assertj.core.groups.Tuple.tuple(1, ActionType.NETWORKING, false),
                org.assertj.core.groups.Tuple.tuple(2, ActionType.REST, true)
            );
        assertThat(found.getPreviewCashChange()).isEqualTo(Money.of(150_000L));
        assertThat(found.getPreviewCashMinChange()).isEqualTo(Money.of(150_000L));
        assertThat(found.getPreviewCashMaxChange()).isEqualTo(Money.of(300_000L));
        assertThat(found.getPreviewStatChanges()).containsEntry("health", 5);
        assertThat(found.getPreviewStatChanges()).containsEntry("knowledge", 8);
        assertThat(stringRedisTemplate.hasKey("session:101:turn")).isTrue();
    }

    @DisplayName("구 스키마 TurnDraft payload도 min/max를 previewCashChange로 보정해 읽는다.")
    @Test
    void findBySessionIdWithLegacyPayload() {
        // given
        stringRedisTemplate.opsForValue().set(
            "session:404:turn",
            """
                {
                  "sessionId":404,
                  "turnNumber":8,
                  "slots":[
                    {"slotIndex":0,"actionType":"REST","forcedAction":true},
                    {"slotIndex":1,"actionType":"STUDY","forcedAction":false},
                    {"slotIndex":2,"actionType":"REST","forcedAction":true}
                  ],
                  "previewCashChangeAmount":120000,
                  "previewStatChanges":{"health":6,"stress":-10}
                }
                """
        );

        // when
        final TurnDraft found = turnDraftRepository.findBySessionId(404L)
            .orElseThrow();

        // then
        assertThat(found.getPreviewCashChange()).isEqualTo(Money.of(120_000L));
        assertThat(found.getPreviewCashMinChange()).isEqualTo(Money.of(120_000L));
        assertThat(found.getPreviewCashMaxChange()).isEqualTo(Money.of(120_000L));
        assertThat(found.getSlots())
            .extracting(TurnDraftSlot::getSlotIndex, TurnDraftSlot::getActionType, TurnDraftSlot::isForcedAction)
            .containsExactly(
                org.assertj.core.groups.Tuple.tuple(0, ActionType.REST, true),
                org.assertj.core.groups.Tuple.tuple(1, ActionType.STUDY, false),
                org.assertj.core.groups.Tuple.tuple(2, ActionType.REST, true)
            );
    }

    @DisplayName("같은 세션의 TurnDraft를 다시 저장하면 기존 값을 덮어쓴다.")
    @Test
    void overwriteTurnDraft() {
        // given
        turnDraftRepository.save(createTurnDraft(
            202L,
            3,
            List.of(TurnDraftSlot.of(0, ActionType.HOBBY)),
            Money.of(-100_000L),
            Map.of("happiness", 6)
        ));
        final TurnDraft latestTurnDraft = createTurnDraft(
            202L,
            4,
            List.of(
                TurnDraftSlot.of(0, ActionType.SIDE_JOB),
                TurnDraftSlot.of(1, ActionType.REST)
            ),
            Money.of(200_000L),
            Map.of("fatigue", -12)
        );

        // when
        turnDraftRepository.save(latestTurnDraft);

        // then
        final TurnDraft found = turnDraftRepository.findBySessionId(202L)
            .orElseThrow();
        assertThat(found.getTurnNumber()).isEqualTo(4);
        assertThat(found.getSlots()).hasSize(2);
        assertThat(found.getPreviewCashChange()).isEqualTo(Money.of(200_000L));
        assertThat(found.getPreviewStatChanges()).containsEntry("fatigue", -12);
    }

    @DisplayName("저장되지 않은 TurnDraft는 조회되지 않는다.")
    @Test
    void returnEmptyWhenTurnDraftDoesNotExist() {
        // when
        final boolean exists = turnDraftRepository.findBySessionId(999L).isPresent();

        // then
        assertThat(exists).isFalse();
    }

    @DisplayName("TurnDraft를 삭제하면 다시 조회되지 않는다.")
    @Test
    void deleteBySessionId() {
        // given
        turnDraftRepository.save(createTurnDraft(
            303L,
            7,
            List.of(TurnDraftSlot.of(0, ActionType.MEET_FRIEND)),
            Money.of(-150_000L),
            Map.of("happiness", 10)
        ));

        // when
        turnDraftRepository.deleteBySessionId(303L);

        // then
        assertThat(turnDraftRepository.findBySessionId(303L)).isEmpty();
        assertThat(stringRedisTemplate.hasKey("session:303:turn")).isFalse();
    }

    private TurnDraft createTurnDraft(
        final Long sessionId,
        final Integer turnNumber,
        final List<TurnDraftSlot> slots,
        final Money previewCashChange,
        final Map<String, Integer> previewStatChanges
    ) {
        return createTurnDraft(
            sessionId,
            turnNumber,
            slots,
            previewCashChange,
            previewCashChange,
            previewCashChange,
            previewStatChanges
        );
    }

    private TurnDraft createTurnDraft(
        final Long sessionId,
        final Integer turnNumber,
        final List<TurnDraftSlot> slots,
        final Money previewCashChange,
        final Money previewCashMinChange,
        final Money previewCashMaxChange,
        final Map<String, Integer> previewStatChanges
    ) {
        return TurnDraft.of(
            sessionId,
            turnNumber,
            slots,
            previewCashChange,
            previewCashMinChange,
            previewCashMaxChange,
            previewStatChanges
        );
    }
}
