package io.ssafy.p.j14c103.homerun.api.service.game.session;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class GameSessionCleanupService {

    private static final List<SessionScopedTable> SESSION_SCOPED_TABLES = List.of(
        new SessionScopedTable("game_timelines", "game_session_id"),
        new SessionScopedTable("game_reports", "game_session_id"),
        new SessionScopedTable("game_play_histories", "game_session_id"),
        new SessionScopedTable("게임플레이이력", "게임번호"),
        new SessionScopedTable("game_event_logs", "game_session_id"),
        new SessionScopedTable("game_pending_events", "game_session_id"),
        new SessionScopedTable("game_news_logs", "game_session_id"),
        new SessionScopedTable("game_cards", "game_session_id"),
        new SessionScopedTable("game_loans", "game_session_id"),
        new SessionScopedTable("loan_applications", "game_session_id"),
        new SessionScopedTable("stock_orders", "game_session_id"),
        new SessionScopedTable("stock_holdings", "game_session_id"),
        new SessionScopedTable("game_stock_market_states", "game_session_id"),
        new SessionScopedTable("game_contract_reviews", "game_session_id"),
        new SessionScopedTable("game_property_market_states", "game_session_id"),
        new SessionScopedTable("game_housings", "game_session_id"),
        new SessionScopedTable("settlement_logs", "game_session_id"),
        new SessionScopedTable("game_turn_slots", "game_session_id"),
        new SessionScopedTable("게임턴슬롯", "게임번호"),
        new SessionScopedTable("game_careers", "game_session_id"),
        new SessionScopedTable("게임커리어", "게임번호"),
        new SessionScopedTable("game_stats", "game_session_id"),
        new SessionScopedTable("게임스탯", "게임번호")
    );

    private final JdbcTemplate jdbcTemplate;

    public void deleteAllByGameSessionId(final Long gameSessionId) {
        final Integer legacyGameId = Math.toIntExact(gameSessionId);

        for (final SessionScopedTable table : SESSION_SCOPED_TABLES) {
            if (!tableExists(table.tableName())) {
                continue;
            }

            final Object keyValue = table.usesLegacyKey() ? legacyGameId : gameSessionId;
            jdbcTemplate.update(
                "delete from " + table.tableName() + " where " + table.keyColumn() + " = ?",
                keyValue
            );
        }
    }

    private boolean tableExists(final String tableName) {
        final Integer count = jdbcTemplate.queryForObject(
            "select count(*) from information_schema.tables where lower(table_name) = lower(?)",
            Integer.class,
            tableName
        );

        return count != null && count > 0;
    }

    private record SessionScopedTable(String tableName, String keyColumn) {

        private boolean usesLegacyKey() {
            return "게임번호".equals(keyColumn);
        }
    }
}
