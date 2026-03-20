package io.ssafy.p.j14c103.homerun.domain.world;

import io.ssafy.p.j14c103.homerun.domain.world.event.EventPresentationType;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventTriggerType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class WorldContentSeedPolicy {

    private static final String IMMEDIATE = "IMMEDIATE";
    private static final String ADD = "ADD";
    private static final String NOTE = "NOTE";
    private static final String COLUMN_COMPARISON = "COLUMN_COMPARISON";
    private static final String EQ = "EQ";
    private static final String GTE = "GTE";
    private static final String AND = "AND";
    private static final String OR = "OR";

    public WorldContentSeedPlan calculate() {
        return new WorldContentSeedPlan(newsSeeds(), eventSeeds());
    }

    public List<NewsSeed> createNewsSeeds(final List<Map<String, Object>> definitions) {
        if (definitions == null || definitions.isEmpty()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }

        return definitions.stream()
            .map(this::toNewsSeed)
            .toList();
    }

    public List<EventSeed> createEventSeeds(final List<Map<String, Object>> definitions) {
        if (definitions == null || definitions.isEmpty()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }

        return definitions.stream()
            .map(this::toEventSeed)
            .toList();
    }

    private NewsSeed toNewsSeed(final Map<String, Object> definition) {
        return new NewsSeed(
            requiredText(definition, "newsId"),
            requiredText(definition, "title"),
            requiredText(definition, "category"),
            requiredText(definition, "sentiment"),
            requiredMap(definition, "sectorImpact"),
            optionalInt(definition, "exchangeRateImpact", 0),
            requiredInt(definition, "realEstateImpact"),
            requiredMap(definition, "jobImpact")
        );
    }

    private EventSeed toEventSeed(final Map<String, Object> definition) {
        try {
            return new EventSeed(
                requiredText(definition, "eventTypeCode", requiredText(definition, "eventCode")),
                requiredText(definition, "eventCode"),
                requiredText(definition, "eventName"),
                EventPresentationType.valueOf(requiredText(definition, "presentationType")),
                EventTriggerType.valueOf(requiredText(definition, "triggerType")),
                optionalBigDecimal(definition, "triggerValue"),
                optionalText(definition, "imageUrl"),
                optionalText(definition, "senderName"),
                optionalText(definition, "receiverName"),
                requiredText(definition, "description", requiredText(definition, "eventName")),
                toChoiceSeeds(requiredList(definition, "choices")),
                toConditionSeeds(optionalList(definition, "conditions")),
                toEffectSeeds(optionalList(definition, "effects"))
            );
        } catch (IllegalArgumentException exception) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID, exception);
        }
    }

    private List<EventChoiceSeed> toChoiceSeeds(final List<?> choices) {
        return choices.stream()
            .map(this::toChoiceSeed)
            .toList();
    }

    private EventChoiceSeed toChoiceSeed(final Object choice) {
        if (!(choice instanceof Map<?, ?> rawChoice)) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> definition = (Map<String, Object>) rawChoice;
        return new EventChoiceSeed(
            requiredText(definition, "choiceCode"),
            requiredText(definition, "choiceName", requiredText(definition, "label")),
            requiredInt(definition, "choiceOrder", 1),
            optionalText(definition, "choiceDescription")
        );
    }

    private List<EventConditionSeed> toConditionSeeds(final List<?> conditions) {
        return conditions.stream()
            .map(this::toConditionSeed)
            .toList();
    }

    private EventConditionSeed toConditionSeed(final Object condition) {
        if (!(condition instanceof Map<?, ?> rawCondition)) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> definition = (Map<String, Object>) rawCondition;
        return new EventConditionSeed(
            optionalInt(definition, "conditionGroupNumber", 1),
            requiredInt(definition, "conditionOrder", 1),
            requiredText(definition, "conditionType"),
            optionalText(definition, "targetTableName"),
            optionalText(definition, "targetColumnName"),
            optionalText(definition, "comparisonOperator"),
            optionalText(definition, "criteriaTextValue"),
            optionalBigDecimal(definition, "criteriaNumberValue1"),
            optionalBigDecimal(definition, "criteriaNumberValue2"),
            optionalText(definition, "logicalOperatorType")
        );
    }

    private List<EventEffectSeed> toEffectSeeds(final List<?> effects) {
        return effects.stream()
            .map(this::toEffectSeed)
            .toList();
    }

    private EventEffectSeed toEffectSeed(final Object effect) {
        if (!(effect instanceof Map<?, ?> rawEffect)) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> definition = (Map<String, Object>) rawEffect;
        return new EventEffectSeed(
            optionalText(definition, "choiceCode"),
            requiredInt(definition, "effectOrder", 1),
            requiredText(definition, "applicationTimingType"),
            optionalText(definition, "targetTableName"),
            optionalText(definition, "targetColumnName"),
            optionalText(definition, "operationType"),
            optionalIntObject(definition, "baseNumberValue"),
            optionalIntObject(definition, "minNumberValue"),
            optionalIntObject(definition, "maxNumberValue"),
            optionalText(definition, "baseTextValue"),
            optionalIntObject(definition, "durationTurns"),
            optionalText(definition, "note")
        );
    }

    private String requiredText(final Map<String, Object> definition, final String key) {
        Object value = definition.get(key);
        if (value instanceof String text && !text.isBlank()) {
            return text;
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    private String requiredText(
        final Map<String, Object> definition,
        final String key,
        final String fallback
    ) {
        Object value = definition.get(key);
        if (value instanceof String text && !text.isBlank()) {
            return text;
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    private String optionalText(final Map<String, Object> definition, final String key) {
        Object value = definition.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof String text) {
            return text;
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    private Map<String, Object> requiredMap(final Map<String, Object> definition, final String key) {
        Object value = definition.get(key);
        if (value instanceof Map<?, ?> rawMap) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) rawMap;
            return map;
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    private List<?> requiredList(final Map<String, Object> definition, final String key) {
        Object value = definition.get(key);
        if (value instanceof List<?> list && !list.isEmpty()) {
            return list;
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    private List<?> optionalList(final Map<String, Object> definition, final String key) {
        Object value = definition.get(key);
        if (value == null) {
            return List.of();
        }
        if (value instanceof List<?> list) {
            return list;
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    private int requiredInt(final Map<String, Object> definition, final String key) {
        Integer value = optionalIntObject(definition, key);
        if (value != null) {
            return value;
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    private int requiredInt(
        final Map<String, Object> definition,
        final String key,
        final int fallback
    ) {
        Integer value = optionalIntObject(definition, key);
        if (value != null) {
            return value;
        }

        return fallback;
    }

    private int optionalInt(
        final Map<String, Object> definition,
        final String key,
        final int fallback
    ) {
        Integer value = optionalIntObject(definition, key);
        if (value != null) {
            return value;
        }

        return fallback;
    }

    private Integer optionalIntObject(final Map<String, Object> definition, final String key) {
        Object value = definition.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Integer integer) {
            return integer;
        }
        if (value instanceof Long longValue) {
            return Math.toIntExact(longValue);
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    private BigDecimal optionalBigDecimal(final Map<String, Object> definition, final String key) {
        Object value = definition.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Integer integer) {
            return BigDecimal.valueOf(integer.longValue());
        }
        if (value instanceof Long longValue) {
            return BigDecimal.valueOf(longValue);
        }
        if (value instanceof Double doubleValue) {
            return BigDecimal.valueOf(doubleValue);
        }
        if (value instanceof String text && !text.isBlank()) {
            return new BigDecimal(text);
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    private List<NewsSeed> newsSeeds() {
        return List.of(
            news("NEWS-001", "기준금리 인상 전망이 확산되며 대출 부담 우려가 커집니다.", "RATE_CHANGE", "NEGATIVE", -2,
                Map.of("FINANCE", 1, "REAL_ESTATE", -2, "BIGTECH", -1), Map.of("SMALL_BIZ", Map.of("layoffMult", 105))),
            news("NEWS-002", "예금 금리 경쟁이 다시 붙으며 금융권 유동성이 커집니다.", "RATE_CHANGE", "POSITIVE", 1,
                Map.of("FINANCE", 2, "CONSUMER", -1), Map.of()),
            news("NEWS-003", "서울 핵심 지역 매수 문의가 늘며 아파트 기대감이 살아납니다.", "REAL_ESTATE", "POSITIVE", 3,
                Map.of("REAL_ESTATE", 3, "FINANCE", 1), Map.of()),
            news("NEWS-004", "전세 수요가 둔화되며 빌라 시장 관망세가 짙어집니다.", "REAL_ESTATE", "NEGATIVE", -2,
                Map.of("REAL_ESTATE", -2, "CONSUMER", -1), Map.of()),
            news("NEWS-005", "스타트업 채용 공고가 늘며 개발직 연봉 기대치가 오릅니다.", "JOB", "POSITIVE", 0,
                Map.of("BIGTECH", 1, "CONSUMER", 1), Map.of("STARTUP", Map.of("salaryMult", 105))),
            news("NEWS-006", "중소기업 채용 시장이 얼어붙으며 이직 경쟁이 치열해집니다.", "JOB", "NEGATIVE", -1,
                Map.of("CONSUMER", -1, "AUTO", -1), Map.of("SMALL_BIZ", Map.of("layoffMult", 110))),
            news("NEWS-007", "수출 회복 기대에 반도체 업종이 강세를 보입니다.", "STOCK", "POSITIVE", 1,
                Map.of("SEMICONDUCTOR", 3, "BIGTECH", 2), Map.of()),
            news("NEWS-008", "글로벌 기술 규제 이슈로 대형 기술주 변동성이 커집니다.", "STOCK", "NEGATIVE", -1,
                Map.of("BIGTECH", -3, "SEMICONDUCTOR", -1), Map.of()),
            news("NEWS-009", "내수 소비가 살아나며 유통 업종 실적 개선 기대가 커집니다.", "ECONOMY", "POSITIVE", 1,
                Map.of("CONSUMER", 3, "FINANCE", 1), Map.of("FREELANCER", Map.of("incomeMult", 103))),
            news("NEWS-010", "물가 부담이 이어지며 가계 소비 심리가 다시 위축됩니다.", "ECONOMY", "NEGATIVE", -1,
                Map.of("CONSUMER", -2, "FINANCE", -1), Map.of()),
            news("NEWS-011", "재건축 기대 지역 중심으로 매수 심리가 다시 고개를 듭니다.", "REAL_ESTATE", "POSITIVE", 2,
                Map.of("REAL_ESTATE", 2, "FINANCE", 1), Map.of()),
            news("NEWS-012", "임대차 분쟁이 늘며 전세 계약 리스크 경계가 커집니다.", "REAL_ESTATE", "NEGATIVE", -2,
                Map.of("REAL_ESTATE", -1, "FINANCE", -1), Map.of()),
            news("NEWS-013", "제조업 가동률이 회복되며 자동차 업종 수익성 기대가 높아집니다.", "STOCK", "POSITIVE", 1,
                Map.of("AUTO", 3, "CONSUMER", 1), Map.of("MID_BIZ", Map.of("salaryMult", 102))),
            news("NEWS-014", "원자재 비용 부담이 커지며 제조업 실적 압박이 거론됩니다.", "STOCK", "NEGATIVE", -1,
                Map.of("AUTO", -2, "CONSUMER", -1), Map.of("MID_BIZ", Map.of("layoffMult", 105))),
            news("NEWS-015", "대기업 공채 확대 소식에 취업 준비생 기대감이 커집니다.", "JOB", "POSITIVE", 0,
                Map.of("BIGTECH", 1, "FINANCE", 1), Map.of("LARGE_BIZ", Map.of("salaryMult", 104))),
            news("NEWS-016", "프리랜서 단가 경쟁이 심화되며 외주 시장이 빡빡해집니다.", "JOB", "NEGATIVE", 0,
                Map.of("BIGTECH", -1, "CONSUMER", -1), Map.of("FREELANCER", Map.of("incomeMult", 95))),
            news("NEWS-017", "청년 지원 대출 완화 기대가 나오며 금융상품 문의가 늘어납니다.", "RATE_CHANGE", "POSITIVE", 1,
                Map.of("FINANCE", 2, "REAL_ESTATE", 1), Map.of()),
            news("NEWS-018", "가계부채 관리 강화 방침에 주택 매수 심리가 조심스러워집니다.", "RATE_CHANGE", "NEGATIVE", -3,
                Map.of("FINANCE", -1, "REAL_ESTATE", -3), Map.of()),
            news("NEWS-019", "도심 소형 주거 선호가 이어지며 원룸 수요가 꾸준히 유지됩니다.", "REAL_ESTATE", "POSITIVE", 1,
                Map.of("REAL_ESTATE", 1, "CONSUMER", 1), Map.of()),
            news("NEWS-020", "경기 둔화 우려로 기업들이 신규 채용과 투자를 함께 줄입니다.", "ECONOMY", "NEGATIVE", -2,
                Map.of("BIGTECH", -1, "FINANCE", -1, "CONSUMER", -2), Map.of("STARTUP", Map.of("layoffMult", 108)))
        );
    }

    private List<EventSeed> eventSeeds() {
        return List.of(
            event(
                "VOICE_PHISHING",
                "EVT-VOICE-001",
                "보이스피싱",
                EventPresentationType.PHONE,
                EventTriggerType.PROBABILITY,
                new BigDecimal("0.0500"),
                "/images/events/phone.png",
                null,
                null,
                "검찰을 사칭하며 계좌 이체를 요구하는 전화가 왔습니다.",
                List.of(
                    choice("A", "무시한다", 1, "수상한 전화는 바로 끊는다."),
                    choice("B", "지시에 따른다", 2, "상대의 요구대로 송금을 진행한다.")
                ),
                List.of(),
                List.of(
                    effect("B", 1, IMMEDIATE, "game_sessions", "cash", ADD, -3_000_000, null,
                        "보이스피싱 피해 금액")
                )
            ),
            event(
                "OVERTIME_REQUEST",
                "EVT-OVERTIME-001",
                "야근 요청",
                EventPresentationType.CHOICE,
                EventTriggerType.CYCLE,
                new BigDecimal("0.1000"),
                "/images/events/overtime.png",
                "팀장",
                "김싸피",
                "마감 일정 때문에 야근 요청이 들어왔습니다.",
                List.of(
                    choice("A", "수락한다", 1, "보상을 받고 오늘 일정을 더 소화한다."),
                    choice("B", "거절한다", 2, "건강을 우선해 야근을 거절한다.")
                ),
                List.of(
                    condition(1, 1, COLUMN_COMPARISON, "game_sessions", "economic_cycle_type", EQ,
                        "BOOM", null, OR),
                    condition(2, 1, COLUMN_COMPARISON, "game_sessions", "economic_cycle_type", EQ,
                        "RECOVERY", null, OR)
                ),
                List.of(
                    effect("A", 1, IMMEDIATE, "game_sessions", "cash", ADD, 300_000, null,
                        "야근 수당 지급"),
                    effect("A", 2, IMMEDIATE, "game_stats", "stress", ADD, 10, null,
                        "야근으로 스트레스 증가"),
                    effect("B", 1, IMMEDIATE, null, null, NOTE, null, null,
                        "호황 시 20%, 회복기 10%는 T14에서 해석"),
                    effect("B", 2, IMMEDIATE, null, null, NOTE, null, null,
                        "거절 시 연봉 협상 페널티는 T18에서 해석")
                )
            ),
            event(
                "FAMILY_EVENT",
                "EVT-FAMILY-001",
                "경조사",
                EventPresentationType.CHOICE,
                EventTriggerType.PROBABILITY,
                new BigDecimal("0.0800"),
                "/images/events/family.png",
                "가족 단톡방",
                "김싸피",
                "이번 주말에 꼭 참석해야 할 가족 경조사가 생겼습니다.",
                List.of(
                    choice("A", "참석한다", 1, "시간과 비용을 써서 자리를 지킨다."),
                    choice("B", "불참한다", 2, "당장 일정과 지출을 아끼기 위해 불참한다.")
                ),
                List.of(),
                List.of(
                    effect("A", 1, IMMEDIATE, "game_sessions", "cash", ADD, -150_000, null,
                        "경조사 참석 비용"),
                    effect("A", 2, IMMEDIATE, "game_stats", "happiness", ADD, 5, null,
                        "관계 유지로 행복도 증가"),
                    effect("B", 1, IMMEDIATE, null, null, NOTE, null, null,
                        "불참 시 관계 악화는 T18에서 해석")
                )
            ),
            event(
                "JOB_TRANSFER",
                "EVT-JOB-001",
                "열심히 일한 당신! 이직하시겠습니까?",
                EventPresentationType.JOB_TRANSFER,
                EventTriggerType.CONDITION,
                null,
                "/images/events/job-transfer.png",
                "OO 기업 인사팀",
                "김싸피 님",
                "열심히 일한 당신에게 새로운 이직 오퍼가 도착했습니다.",
                List.of(
                    choice("ACCEPT", "승인하기", 1, "더 나은 오퍼를 받아들인다."),
                    choice("REJECT", "거절하기", 2, "현재 직장에 남는다.")
                ),
                List.of(
                    condition(1, 1, COLUMN_COMPARISON, "game_stats", "knowledge", GTE,
                        null, new BigDecimal("60"), AND),
                    condition(1, 2, COLUMN_COMPARISON, "game_careers", "tenure_turns", GTE,
                        null, new BigDecimal("12"), AND)
                ),
                List.of(
                    effect("ACCEPT", 1, IMMEDIATE, null, null, NOTE, null, null,
                        "실제 오퍼 계산과 근속 리셋은 Domain B/T18에서 해석"),
                    effect("REJECT", 1, IMMEDIATE, null, null, NOTE, null, null,
                        "거절 시 현재 직장 유지")
                )
            )
        );
    }

    private NewsSeed news(
        final String newsId,
        final String title,
        final String category,
        final String sentiment,
        final Integer realEstateImpact,
        final Map<String, Object> sectorImpact,
        final Map<String, Object> jobImpact
    ) {
        return new NewsSeed(
            newsId,
            title,
            category,
            sentiment,
            sectorImpact,
            0,
            realEstateImpact,
            jobImpact
        );
    }

    private EventSeed event(
        final String eventTypeCode,
        final String eventCode,
        final String eventName,
        final EventPresentationType presentationType,
        final EventTriggerType triggerType,
        final BigDecimal triggerValue,
        final String imageUrl,
        final String senderName,
        final String receiverName,
        final String description,
        final List<EventChoiceSeed> choices,
        final List<EventConditionSeed> conditions,
        final List<EventEffectSeed> effects
    ) {
        return new EventSeed(
            eventTypeCode,
            eventCode,
            eventName,
            presentationType,
            triggerType,
            triggerValue,
            imageUrl,
            senderName,
            receiverName,
            description,
            choices,
            conditions,
            effects
        );
    }

    private EventChoiceSeed choice(
        final String choiceCode,
        final String choiceName,
        final Integer choiceOrder,
        final String choiceDescription
    ) {
        return new EventChoiceSeed(choiceCode, choiceName, choiceOrder, choiceDescription);
    }

    private EventConditionSeed condition(
        final Integer conditionGroupNumber,
        final Integer conditionOrder,
        final String conditionType,
        final String targetTableName,
        final String targetColumnName,
        final String comparisonOperator,
        final String criteriaTextValue,
        final BigDecimal criteriaNumberValue1,
        final String logicalOperatorType
    ) {
        return new EventConditionSeed(
            conditionGroupNumber,
            conditionOrder,
            conditionType,
            targetTableName,
            targetColumnName,
            comparisonOperator,
            criteriaTextValue,
            criteriaNumberValue1,
            null,
            logicalOperatorType
        );
    }

    private EventEffectSeed effect(
        final String choiceCode,
        final Integer effectOrder,
        final String applicationTimingType,
        final String targetTableName,
        final String targetColumnName,
        final String operationType,
        final Integer baseNumberValue,
        final Integer durationTurns,
        final String note
    ) {
        return new EventEffectSeed(
            choiceCode,
            effectOrder,
            applicationTimingType,
            targetTableName,
            targetColumnName,
            operationType,
            baseNumberValue,
            null,
            null,
            null,
            durationTurns,
            note
        );
    }

    public record WorldContentSeedPlan(
        List<NewsSeed> newsSeeds,
        List<EventSeed> eventSeeds
    ) {

        public WorldContentSeedPlan {
            if (newsSeeds == null || newsSeeds.size() != 20) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
            if (eventSeeds == null || eventSeeds.size() != 4) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
        }
    }

    public record NewsSeed(
        String newsId,
        String title,
        String category,
        String sentiment,
        Map<String, Object> sectorImpact,
        Integer exchangeRateImpact,
        Integer realEstateImpact,
        Map<String, Object> jobImpact
    ) {

        public NewsSeed {
            if (isBlank(newsId) || isBlank(title)) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
            if (isBlank(category) || isBlank(sentiment)) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
            if (sectorImpact == null || jobImpact == null) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
            if (exchangeRateImpact == null || realEstateImpact == null) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
        }
    }

    public record EventSeed(
        String eventTypeCode,
        String eventCode,
        String eventName,
        EventPresentationType presentationType,
        EventTriggerType triggerType,
        BigDecimal triggerValue,
        String imageUrl,
        String senderName,
        String receiverName,
        String description,
        List<EventChoiceSeed> choices,
        List<EventConditionSeed> conditions,
        List<EventEffectSeed> effects
    ) {

        public EventSeed {
            if (isBlank(eventTypeCode) || isBlank(eventCode)) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
            if (isBlank(eventName) || isBlank(description)) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
            if (presentationType == null || triggerType == null) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
            if (choices == null || choices.isEmpty()) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
            if (conditions == null || effects == null) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
            if (triggerType == EventTriggerType.CONDITION && conditions.isEmpty()) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
            if (triggerType == EventTriggerType.CYCLE && conditions.isEmpty()) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
        }
    }

    public record EventChoiceSeed(
        String choiceCode,
        String choiceName,
        Integer choiceOrder,
        String choiceDescription
    ) {

        public EventChoiceSeed {
            if (isBlank(choiceCode) || isBlank(choiceName)) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
            if (choiceOrder == null || choiceOrder < 1) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
        }
    }

    public record EventConditionSeed(
        Integer conditionGroupNumber,
        Integer conditionOrder,
        String conditionType,
        String targetTableName,
        String targetColumnName,
        String comparisonOperator,
        String criteriaTextValue,
        BigDecimal criteriaNumberValue1,
        BigDecimal criteriaNumberValue2,
        String logicalOperatorType
    ) {

        public EventConditionSeed {
            if (conditionOrder == null || conditionOrder < 1) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
            if (isBlank(conditionType)) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
        }
    }

    public record EventEffectSeed(
        String choiceCode,
        Integer effectOrder,
        String applicationTimingType,
        String targetTableName,
        String targetColumnName,
        String operationType,
        Integer baseNumberValue,
        Integer minNumberValue,
        Integer maxNumberValue,
        String baseTextValue,
        Integer durationTurns,
        String note
    ) {

        public EventEffectSeed {
            if (effectOrder == null || effectOrder < 1) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
            if (isBlank(applicationTimingType) || isBlank(operationType)) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
        }
    }

    private static boolean isBlank(final String value) {
        return value == null || value.isBlank();
    }
}
