package io.ssafy.p.j14c103.homerun.api.service.game.session.response;

import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
public class GameSessionListResponse {

    private List<SessionSummaryResponse> sessions;

    @Builder
    private GameSessionListResponse(final List<SessionSummaryResponse> sessions) {
        this.sessions = sessions;
    }

    public static GameSessionListResponse of(final List<SessionSummaryResponse> sessions) {
        return GameSessionListResponse.builder()
            .sessions(sessions)
            .build();
    }

    public static GameSessionListResponse from(final List<GameSession> gameSessions) {
        final Map<Integer, GameSession> sessionBySlot = new HashMap<>();
        for (final GameSession gameSession : gameSessions) {
            sessionBySlot.put(gameSession.getSlotNumber(), gameSession);
        }

        final List<SessionSummaryResponse> sessions = new ArrayList<>();
        for (int slotNumber = 1; slotNumber <= 3; slotNumber++) {
            final GameSession gameSession = sessionBySlot.get(slotNumber);
            if (gameSession == null) {
                sessions.add(SessionSummaryResponse.empty(slotNumber));
                continue;
            }
            sessions.add(SessionSummaryResponse.from(gameSession));
        }

        return GameSessionListResponse.of(sessions);
    }

    @Getter
    public static class SessionSummaryResponse {

        private Long sessionId;
        private Integer slotNumber;
        private String status;
        private String characterName;
        private CharacterType characterType;
        private JobType jobType;
        private Integer currentTurn;
        private Long totalAssets;
        private LocalDateTime createdAt;

        @Builder
        private SessionSummaryResponse(
            final Long sessionId,
            final Integer slotNumber,
            final String status,
            final String characterName,
            final CharacterType characterType,
            final JobType jobType,
            final Integer currentTurn,
            final Long totalAssets,
            final LocalDateTime createdAt
        ) {
            this.sessionId = sessionId;
            this.slotNumber = slotNumber;
            this.status = status;
            this.characterName = characterName;
            this.characterType = characterType;
            this.jobType = jobType;
            this.currentTurn = currentTurn;
            this.totalAssets = totalAssets;
            this.createdAt = createdAt;
        }

        public static SessionSummaryResponse of(
            final Long sessionId,
            final Integer slotNumber,
            final String status,
            final String characterName,
            final CharacterType characterType,
            final JobType jobType,
            final Integer currentTurn,
            final Long totalAssets,
            final LocalDateTime createdAt
        ) {
            return SessionSummaryResponse.builder()
                .sessionId(sessionId)
                .slotNumber(slotNumber)
                .status(status)
                .characterName(characterName)
                .characterType(characterType)
                .jobType(jobType)
                .currentTurn(currentTurn)
                .totalAssets(totalAssets)
                .createdAt(createdAt)
                .build();
        }

        public static SessionSummaryResponse empty(final Integer slotNumber) {
            return SessionSummaryResponse.of(null, slotNumber, "EMPTY", null, null, null, null, null, null);
        }

        public static SessionSummaryResponse from(final GameSession gameSession) {
            return SessionSummaryResponse.of(
                gameSession.getGameSessionId(),
                gameSession.getSlotNumber(),
                gameSession.getSessionStatus().name(),
                gameSession.getCharacterName(),
                gameSession.getCharacterType(),
                gameSession.getJobType(),
                gameSession.getCurrentTurn(),
                gameSession.getNetWorth().getAmount().longValue(),
                gameSession.getCreatedAt()
            );
        }
    }
}
