package io.ssafy.p.j14c103.homerun.api.service.character.history;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareer;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.character.career.SalaryNegotiationPolicy;
import io.ssafy.p.j14c103.homerun.domain.history.GameplayHistory;
import io.ssafy.p.j14c103.homerun.domain.history.GameplayHistoryRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameplayHistoryWriter {

    private static final int STAT_CHANGE_EVENT_ID = 910_012;
    private static final int SALARY_NEGOTIATION_EVENT_ID = 910_014;
    private static final int JOB_TRANSFER_EVENT_ID = 910_016;
    private static final int FORCED_RESIGNATION_EVENT_ID = 910_017;

    private static final String GAME_STAT_TABLE_NAME = "게임스탯";
    private static final String GAME_CAREER_TABLE_NAME = "게임커리어";
    private static final String STAT_COLUMN_NAMES = "체력,피로도,스트레스,행복도,지식,번아웃여부,번아웃시작턴,입원종료턴";
    private static final String SALARY_NEGOTIATION_COLUMN_NAMES = "연봉,마지막협상턴";
    private static final String JOB_TRANSFER_COLUMN_NAMES = "직업유형,직급,연봉,근속턴수,고용상태,수습종료턴,재취업가능턴,실업급여잔여턴수,퇴사전연봉";
    private static final String FORCED_RESIGNATION_COLUMN_NAMES = "고용상태,수습종료턴,재취업가능턴,실업급여잔여턴수,퇴사전연봉";
    private static final String STAT_CHANGE_SUMMARY = "스탯 변경 결과를 반영했습니다.";
    private static final String FORCED_RESIGNATION_SUMMARY = "건강 악화로 강제 퇴사했습니다.";

    private final GameplayHistoryRepository gameplayHistoryRepository;
    private final ObjectMapper objectMapper;

    public GameplayHistory writeStatChange(
        final StatSnapshot beforeStat,
        final GameStat afterStat,
        final int occurredTurn
    ) {
        validateStatRequest(beforeStat, afterStat, occurredTurn);

        final StatSnapshot afterStatSnapshot = StatSnapshot.from(afterStat);
        return gameplayHistoryRepository.save(GameplayHistory.builder()
            .gameId(afterStatSnapshot.getGameId())
            .eventId(STAT_CHANGE_EVENT_ID)
            .tableName(GAME_STAT_TABLE_NAME)
            .columnName(STAT_COLUMN_NAMES)
            .targetKey1(String.valueOf(afterStatSnapshot.getGameId()))
            .beforeValue(toJson(beforeStat.toMap()))
            .afterValue(toJson(afterStatSnapshot.toMap()))
            .effectPayload(toJson(buildStatDelta(beforeStat, afterStatSnapshot)))
            .summary(STAT_CHANGE_SUMMARY)
            .occurredTurn(occurredTurn)
            .build());
    }

    public GameplayHistory writeSalaryNegotiation(
        final CareerSnapshot beforeCareer,
        final GameCareer afterCareer,
        final SalaryNegotiationPolicy.NegotiationResult negotiationResult
    ) {
        validateCareerRequest(beforeCareer, afterCareer);
        if (negotiationResult == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }

        final CareerSnapshot afterCareerSnapshot = CareerSnapshot.from(afterCareer);
        return gameplayHistoryRepository.save(GameplayHistory.builder()
            .gameId(afterCareerSnapshot.getGameId())
            .eventId(SALARY_NEGOTIATION_EVENT_ID)
            .tableName(GAME_CAREER_TABLE_NAME)
            .columnName(SALARY_NEGOTIATION_COLUMN_NAMES)
            .targetKey1(String.valueOf(afterCareerSnapshot.getGameId()))
            .beforeValue(toJson(buildSalaryNegotiationState(beforeCareer)))
            .afterValue(toJson(buildSalaryNegotiationState(afterCareerSnapshot)))
            .effectPayload(toJson(buildSalaryNegotiationEffects(negotiationResult)))
            .summary(negotiationResult.message())
            .occurredTurn(negotiationResult.lastNegotiatedTurn())
            .build());
    }

    public GameplayHistory writeJobTransfer(
        final CareerSnapshot beforeCareer,
        final GameCareer afterCareer,
        final int occurredTurn,
        final String summary
    ) {
        validateCareerRequest(beforeCareer, afterCareer);
        validateOccurredTurn(occurredTurn);
        validateSummary(summary);

        final CareerSnapshot afterCareerSnapshot = CareerSnapshot.from(afterCareer);
        return gameplayHistoryRepository.save(GameplayHistory.builder()
            .gameId(afterCareerSnapshot.getGameId())
            .eventId(JOB_TRANSFER_EVENT_ID)
            .tableName(GAME_CAREER_TABLE_NAME)
            .columnName(JOB_TRANSFER_COLUMN_NAMES)
            .targetKey1(String.valueOf(afterCareerSnapshot.getGameId()))
            .beforeValue(toJson(beforeCareer.toJobTransferMap()))
            .afterValue(toJson(afterCareerSnapshot.toJobTransferMap()))
            .effectPayload(toJson(buildJobTransferEffects(beforeCareer, afterCareerSnapshot)))
            .summary(summary)
            .occurredTurn(occurredTurn)
            .build());
    }

    public GameplayHistory writeForcedResignation(
        final CareerSnapshot beforeCareer,
        final GameCareer afterCareer,
        final int occurredTurn
    ) {
        validateCareerRequest(beforeCareer, afterCareer);
        validateOccurredTurn(occurredTurn);

        final CareerSnapshot afterCareerSnapshot = CareerSnapshot.from(afterCareer);
        return gameplayHistoryRepository.save(GameplayHistory.builder()
            .gameId(afterCareerSnapshot.getGameId())
            .eventId(FORCED_RESIGNATION_EVENT_ID)
            .tableName(GAME_CAREER_TABLE_NAME)
            .columnName(FORCED_RESIGNATION_COLUMN_NAMES)
            .targetKey1(String.valueOf(afterCareerSnapshot.getGameId()))
            .beforeValue(toJson(beforeCareer.toForcedResignationMap()))
            .afterValue(toJson(afterCareerSnapshot.toForcedResignationMap()))
            .effectPayload(toJson(buildForcedResignationEffects(beforeCareer, afterCareerSnapshot)))
            .summary(FORCED_RESIGNATION_SUMMARY)
            .occurredTurn(occurredTurn)
            .build());
    }

    private void validateStatRequest(
        final StatSnapshot beforeStat,
        final GameStat afterStat,
        final int occurredTurn
    ) {
        if (beforeStat == null || afterStat == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
        validateOccurredTurn(occurredTurn);
    }

    private void validateCareerRequest(
        final CareerSnapshot beforeCareer,
        final GameCareer afterCareer
    ) {
        if (beforeCareer == null || afterCareer == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
    }

    private void validateOccurredTurn(final int occurredTurn) {
        if (occurredTurn < 1) {
            throw new HomerunException(ErrorCode.CHARACTER_TURN_INVALID);
        }
    }

    private void validateSummary(final String summary) {
        if (summary == null || summary.isBlank()) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
    }

    private Map<String, Object> buildStatDelta(
        final StatSnapshot beforeStat,
        final StatSnapshot afterStat
    ) {
        final Map<String, Object> delta = new LinkedHashMap<>();
        delta.put("healthDelta", afterStat.getHealth() - beforeStat.getHealth());
        delta.put("fatigueDelta", afterStat.getFatigue() - beforeStat.getFatigue());
        delta.put("stressDelta", afterStat.getStress() - beforeStat.getStress());
        delta.put("happinessDelta", afterStat.getHappiness() - beforeStat.getHappiness());
        delta.put("knowledgeDelta", afterStat.getKnowledge() - beforeStat.getKnowledge());
        delta.put("burnoutChanged", beforeStat.getBurnout() != afterStat.getBurnout());
        delta.put(
            "hospitalizedUntilTurnChanged",
            !isSame(
                beforeStat.getHospitalizedUntilTurn(),
                afterStat.getHospitalizedUntilTurn()
            )
        );
        return delta;
    }

    private Map<String, Object> buildSalaryNegotiationState(final CareerSnapshot careerSnapshot) {
        final Map<String, Object> state = new LinkedHashMap<>();
        state.put("salary", careerSnapshot.getSalary());
        state.put("lastNegotiatedTurn", careerSnapshot.getLastNegotiatedTurn());
        return state;
    }

    private Map<String, Object> buildSalaryNegotiationEffects(
        final SalaryNegotiationPolicy.NegotiationResult negotiationResult
    ) {
        final Map<String, Object> effects = new LinkedHashMap<>();
        effects.put("raiseRate", negotiationResult.raiseRate());
        effects.put(
            "salaryChange",
            negotiationResult.newSalary() - negotiationResult.previousSalary()
        );
        effects.put("lastNegotiatedTurn", negotiationResult.lastNegotiatedTurn());
        return effects;
    }

    private Map<String, Object> buildJobTransferEffects(
        final CareerSnapshot beforeCareer,
        final CareerSnapshot afterCareer
    ) {
        final Map<String, Object> effects = new LinkedHashMap<>();
        effects.put("previousJobType", beforeCareer.getJobType());
        effects.put("newJobType", afterCareer.getJobType());
        effects.put("salaryChange", afterCareer.getSalary() - beforeCareer.getSalary());
        effects.put("tenureReset", afterCareer.getTenureTurns() == 0);
        effects.put(
            "employmentStatusChanged",
            beforeCareer.getEmploymentStatus() != afterCareer.getEmploymentStatus()
        );
        return effects;
    }

    private Map<String, Object> buildForcedResignationEffects(
        final CareerSnapshot beforeCareer,
        final CareerSnapshot afterCareer
    ) {
        final Map<String, Object> effects = new LinkedHashMap<>();
        effects.put("previousEmploymentStatus", beforeCareer.getEmploymentStatus());
        effects.put("employmentStatus", afterCareer.getEmploymentStatus());
        effects.put("rehireAvailableTurn", afterCareer.getRehireAvailableTurn());
        effects.put(
            "remainingUnemploymentBenefitTurns",
            afterCareer.getRemainingUnemploymentBenefitTurns()
        );
        effects.put("salaryBeforeResignation", afterCareer.getSalaryBeforeResignation());
        return effects;
    }

    private String toJson(final Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new HomerunException(ErrorCode.GLOBAL_SERIALIZATION_ERROR, exception);
        }
    }

    private boolean isSame(final Object left, final Object right) {
        if (left == null) {
            return right == null;
        }

        return left.equals(right);
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class StatSnapshot {

        private final Integer gameId;
        private final Integer health;
        private final Integer fatigue;
        private final Integer stress;
        private final Integer happiness;
        private final Integer knowledge;
        private final Boolean burnout;
        private final Integer burnoutStartedTurn;
        private final Integer hospitalizedUntilTurn;

        public static StatSnapshot from(final GameStat gameStat) {
            if (gameStat == null) {
                throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
            }

            return new StatSnapshot(
                gameStat.getGameId(),
                gameStat.getHealth(),
                gameStat.getFatigue(),
                gameStat.getStress(),
                gameStat.getHappiness(),
                gameStat.getKnowledge(),
                gameStat.getBurnout(),
                gameStat.getBurnoutStartedTurn(),
                gameStat.getHospitalizedUntilTurn()
            );
        }

        private Map<String, Object> toMap() {
            final Map<String, Object> state = new LinkedHashMap<>();
            state.put("health", getHealth());
            state.put("fatigue", getFatigue());
            state.put("stress", getStress());
            state.put("happiness", getHappiness());
            state.put("knowledge", getKnowledge());
            state.put("burnout", getBurnout());
            state.put("burnoutStartedTurn", getBurnoutStartedTurn());
            state.put("hospitalizedUntilTurn", getHospitalizedUntilTurn());
            return state;
        }
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class CareerSnapshot {

        private final Integer gameId;
        private final JobType jobType;
        private final String jobTitle;
        private final Integer salary;
        private final Integer tenureTurns;
        private final Integer lastNegotiatedTurn;
        private final EmploymentStatus employmentStatus;
        private final Integer probationEndTurn;
        private final Integer rehireAvailableTurn;
        private final Integer remainingUnemploymentBenefitTurns;
        private final Integer salaryBeforeResignation;

        public static CareerSnapshot from(final GameCareer gameCareer) {
            if (gameCareer == null) {
                throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
            }

            return new CareerSnapshot(
                gameCareer.getGameId(),
                gameCareer.getJobType(),
                gameCareer.getJobTitle(),
                gameCareer.getSalary(),
                gameCareer.getTenureTurns(),
                gameCareer.getLastNegotiatedTurn(),
                gameCareer.getEmploymentStatus(),
                gameCareer.getProbationEndTurn(),
                gameCareer.getRehireAvailableTurn(),
                gameCareer.getRemainingUnemploymentBenefitTurns(),
                gameCareer.getSalaryBeforeResignation()
            );
        }

        private Map<String, Object> toJobTransferMap() {
            final Map<String, Object> state = new LinkedHashMap<>();
            state.put("jobType", getJobType());
            state.put("jobTitle", getJobTitle());
            state.put("salary", getSalary());
            state.put("tenureTurns", getTenureTurns());
            state.put("employmentStatus", getEmploymentStatus());
            state.put("probationEndTurn", getProbationEndTurn());
            state.put("rehireAvailableTurn", getRehireAvailableTurn());
            state.put(
                "remainingUnemploymentBenefitTurns",
                getRemainingUnemploymentBenefitTurns()
            );
            state.put("salaryBeforeResignation", getSalaryBeforeResignation());
            return state;
        }

        private Map<String, Object> toForcedResignationMap() {
            final Map<String, Object> state = new LinkedHashMap<>();
            state.put("employmentStatus", getEmploymentStatus());
            state.put("probationEndTurn", getProbationEndTurn());
            state.put("rehireAvailableTurn", getRehireAvailableTurn());
            state.put(
                "remainingUnemploymentBenefitTurns",
                getRemainingUnemploymentBenefitTurns()
            );
            state.put("salaryBeforeResignation", getSalaryBeforeResignation());
            return state;
        }
    }
}
