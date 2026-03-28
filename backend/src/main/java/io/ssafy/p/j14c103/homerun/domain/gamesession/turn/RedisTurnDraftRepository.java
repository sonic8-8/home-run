package io.ssafy.p.j14c103.homerun.domain.gamesession.turn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class RedisTurnDraftRepository implements TurnDraftRepository {

    private static final String KEY_FORMAT = "session:%d:turn";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void save(final TurnDraft turnDraft) {
        stringRedisTemplate.opsForValue().set(
            key(turnDraft.getSessionId()),
            serialize(turnDraft)
        );
    }

    @Override
    public Optional<TurnDraft> findBySessionId(final Long sessionId) {
        validateSessionId(sessionId);

        final String value = stringRedisTemplate.opsForValue().get(key(sessionId));

        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(deserialize(value));
    }

    @Override
    public void deleteBySessionId(final Long sessionId) {
        validateSessionId(sessionId);
        stringRedisTemplate.delete(key(sessionId));
    }

    private String key(final Long sessionId) {
        validateSessionId(sessionId);
        return KEY_FORMAT.formatted(sessionId);
    }

    private void validateSessionId(final Long sessionId) {
        if (sessionId == null || sessionId <= 0L) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private String serialize(final TurnDraft turnDraft) {
        try {
            return objectMapper.writeValueAsString(TurnDraftCacheValue.from(turnDraft));
        } catch (JsonProcessingException exception) {
            throw new HomerunException(ErrorCode.GLOBAL_SERIALIZATION_ERROR, exception);
        }
    }

    private TurnDraft deserialize(final String value) {
        try {
            final TurnDraftCacheValue cacheValue = objectMapper.readValue(
                value,
                TurnDraftCacheValue.class
            );
            return cacheValue.toDomain();
        } catch (JsonProcessingException exception) {
            throw new HomerunException(ErrorCode.GLOBAL_SERIALIZATION_ERROR, exception);
        }
    }

    private record TurnDraftCacheValue(
        Long sessionId,
        Integer turnNumber,
        List<TurnDraftSlotCacheValue> slots,
        Long previewCashChangeAmount,
        Map<String, Integer> previewStatChanges
    ) {

        private static TurnDraftCacheValue from(final TurnDraft turnDraft) {
            return new TurnDraftCacheValue(
                turnDraft.getSessionId(),
                turnDraft.getTurnNumber(),
                turnDraft.getSlots().stream()
                    .map(TurnDraftSlotCacheValue::from)
                    .toList(),
                turnDraft.getPreviewCashChange().getAmount().longValueExact(),
                turnDraft.getPreviewStatChanges()
            );
        }

        private TurnDraft toDomain() {
            return TurnDraft.of(
                sessionId,
                turnNumber,
                slots.stream()
                    .map(TurnDraftSlotCacheValue::toDomain)
                    .toList(),
                Money.of(previewCashChangeAmount),
                previewStatChanges
            );
        }
    }

    private record TurnDraftSlotCacheValue(
        Integer slotIndex,
        ActionType actionType
    ) {

        private static TurnDraftSlotCacheValue from(final TurnDraftSlot turnDraftSlot) {
            return new TurnDraftSlotCacheValue(
                turnDraftSlot.getSlotIndex(),
                turnDraftSlot.getActionType()
            );
        }

        private TurnDraftSlot toDomain() {
            return TurnDraftSlot.of(slotIndex, actionType);
        }
    }
}
