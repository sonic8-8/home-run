package io.ssafy.p.j14c103.homerun.api.service.world.housing.history;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ssafy.p.j14c103.homerun.domain.gamesession.housing.GameHousing;
import io.ssafy.p.j14c103.homerun.domain.history.GameplayHistory;
import io.ssafy.p.j14c103.homerun.domain.history.GameplayHistoryRepository;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HousingGameplayHistoryWriter {

    private static final int HOUSING_CHANGE_EVENT_ID = 920_001;
    private static final String GAME_HOUSING_TABLE_NAME = "게임주거";
    private static final String HOUSING_COLUMN_NAMES = "주거형태,보증금,월세,관리비,현재매물ID";

    private final GameplayHistoryRepository gameplayHistoryRepository;
    private final ObjectMapper objectMapper;

    public GameplayHistory writeHousingChange(
        final GameHousing beforeHousing,
        final GameHousing afterHousing,
        final int occurredTurn,
        final String summary
    ) {
        validateRequest(beforeHousing, afterHousing, occurredTurn, summary);

        return gameplayHistoryRepository.save(GameplayHistory.builder()
            .gameId(Math.toIntExact(afterHousing.getGameSessionId()))
            .eventId(HOUSING_CHANGE_EVENT_ID)
            .tableName(GAME_HOUSING_TABLE_NAME)
            .columnName(HOUSING_COLUMN_NAMES)
            .targetKey1(String.valueOf(afterHousing.getGameSessionId()))
            .beforeValue(toJson(toHousingState(beforeHousing)))
            .afterValue(toJson(toHousingState(afterHousing)))
            .summary(summary)
            .occurredTurn(occurredTurn)
            .build());
    }

    private void validateRequest(
        final GameHousing beforeHousing,
        final GameHousing afterHousing,
        final int occurredTurn,
        final String summary
    ) {
        if (beforeHousing == null || afterHousing == null) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (!beforeHousing.getGameSessionId().equals(afterHousing.getGameSessionId())) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (occurredTurn < 1) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (summary == null || summary.isBlank()) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private Map<String, Object> toHousingState(final GameHousing gameHousing) {
        final Map<String, Object> state = new LinkedHashMap<>();
        state.put("housingType", gameHousing.getCurrentHousingType());
        state.put("depositAmount", resolveMoney(gameHousing.getCurrentDeposit()));
        state.put("monthlyRentAmount", resolveMoney(gameHousing.getMonthlyRent()));
        state.put("maintenanceFeeAmount", resolveMoney(gameHousing.getMaintenanceFee()));
        state.put("propertyId", gameHousing.getCurrentPropertyId());
        return state;
    }

    private Long resolveMoney(final Money money) {
        if (money == null) {
            return null;
        }
        return money.getAmount().longValueExact();
    }

    private String toJson(final Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new HomerunException(ErrorCode.GLOBAL_SERIALIZATION_ERROR, exception);
        }
    }
}
