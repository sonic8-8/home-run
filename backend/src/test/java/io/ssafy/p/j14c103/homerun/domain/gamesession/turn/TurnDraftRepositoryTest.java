package io.ssafy.p.j14c103.homerun.domain.gamesession.turn;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.domain.money.Money;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@ActiveProfiles("test")
@Tag("container")
@Testcontainers(disabledWithoutDocker = true)
class TurnDraftRepositoryTest {

    @Container
    private static final GenericContainer<?> REDIS_CONTAINER = new GenericContainer<>(
        DockerImageName.parse("redis:7.2-alpine")
    ).withExposedPorts(6379);

    @Autowired
    private TurnDraftRepository turnDraftRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @DynamicPropertySource
    static void overrideRedisProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", REDIS_CONTAINER::getFirstMappedPort);
    }

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
                TurnDraftSlot.of(0, ActionType.STUDY),
                TurnDraftSlot.of(1, ActionType.NETWORKING),
                TurnDraftSlot.of(2, ActionType.REST)
            ),
            Money.of(150_000L),
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
            .extracting(TurnDraftSlot::getSlotIndex, TurnDraftSlot::getActionType)
            .containsExactly(
                org.assertj.core.groups.Tuple.tuple(0, ActionType.STUDY),
                org.assertj.core.groups.Tuple.tuple(1, ActionType.NETWORKING),
                org.assertj.core.groups.Tuple.tuple(2, ActionType.REST)
            );
        assertThat(found.getPreviewCashChange()).isEqualTo(Money.of(150_000L));
        assertThat(found.getPreviewStatChanges()).containsEntry("health", 5);
        assertThat(found.getPreviewStatChanges()).containsEntry("knowledge", 8);
        assertThat(stringRedisTemplate.hasKey("session:101:turn")).isTrue();
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
        return TurnDraft.of(
            sessionId,
            turnNumber,
            slots,
            previewCashChange,
            previewStatChanges
        );
    }
}
