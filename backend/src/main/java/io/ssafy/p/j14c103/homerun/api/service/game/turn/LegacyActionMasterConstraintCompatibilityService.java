package io.ssafy.p.j14c103.homerun.api.service.game.turn;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class LegacyActionMasterConstraintCompatibilityService implements ApplicationRunner {

    private static final String GAME_TURN_SLOTS_TABLE = "game_turn_slots";
    private static final String LEGACY_CONSTRAINT = "fk_game_turn_slots__action_master";
    private static final String ACTION_MASTERS_TABLE = "action_masters";

    private final DataSource dataSource;

    @Override
    public void run(final ApplicationArguments args) {
        ensureCompatibleSchema();
    }

    void ensureCompatibleSchema() {
        try (Connection connection = dataSource.getConnection()) {
            final Set<LegacyConstraintRef> legacyConstraints = findLegacyConstraints(connection.getMetaData());
            if (legacyConstraints.isEmpty()) {
                return;
            }

            dropLegacyConstraints(connection, legacyConstraints);
        } catch (final SQLException exception) {
            throw new IllegalStateException("Failed to clean up legacy action_masters constraints.", exception);
        }
    }

    private Set<LegacyConstraintRef> findLegacyConstraints(final DatabaseMetaData metaData) throws SQLException {
        final LinkedHashSet<LegacyConstraintRef> legacyConstraints = new LinkedHashSet<>();
        legacyConstraints.addAll(findLegacyConstraints(metaData, GAME_TURN_SLOTS_TABLE));
        legacyConstraints.addAll(findLegacyConstraints(metaData, GAME_TURN_SLOTS_TABLE.toUpperCase()));
        return legacyConstraints;
    }

    private Set<LegacyConstraintRef> findLegacyConstraints(
        final DatabaseMetaData metaData,
        final String gameTurnSlotsTable
    ) throws SQLException {
        final LinkedHashSet<LegacyConstraintRef> legacyConstraints = new LinkedHashSet<>();

        try (ResultSet importedKeys = metaData.getImportedKeys(null, null, gameTurnSlotsTable)) {
            while (importedKeys.next()) {
                if (isLegacyActionMasterConstraint(importedKeys)) {
                    legacyConstraints.add(new LegacyConstraintRef(
                        importedKeys.getString("FKTABLE_NAME"),
                        extractLegacyConstraintName(importedKeys)
                    ));
                }
            }
        }

        return legacyConstraints;
    }

    private String extractLegacyConstraintName(final ResultSet importedKeys) throws SQLException {
        final String foreignKeyName = importedKeys.getString("FK_NAME");
        final String parentTableName = importedKeys.getString("PKTABLE_NAME");

        if (LEGACY_CONSTRAINT.equalsIgnoreCase(foreignKeyName)) {
            return foreignKeyName;
        }

        if (!ACTION_MASTERS_TABLE.equalsIgnoreCase(parentTableName)) {
            return null;
        }

        if (foreignKeyName == null || foreignKeyName.isBlank()) {
            return LEGACY_CONSTRAINT;
        }

        return foreignKeyName;
    }

    private boolean isLegacyActionMasterConstraint(final ResultSet importedKeys) throws SQLException {
        final String foreignKeyName = importedKeys.getString("FK_NAME");
        final String parentTableName = importedKeys.getString("PKTABLE_NAME");

        if (LEGACY_CONSTRAINT.equalsIgnoreCase(foreignKeyName)) {
            return true;
        }

        return ACTION_MASTERS_TABLE.equalsIgnoreCase(parentTableName);
    }

    private void dropLegacyConstraints(
        final Connection connection,
        final Set<LegacyConstraintRef> legacyConstraints
    ) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            for (LegacyConstraintRef legacyConstraint : legacyConstraints) {
                statement.execute(dropConstraintSql(legacyConstraint));
            }
        }

        log.warn(
            "Dropped legacy action_masters foreign key during startup compatibility cleanup. constraints={}",
            legacyConstraints
        );
    }

    private String dropConstraintSql(final LegacyConstraintRef legacyConstraint) {
        return "alter table if exists "
            + quoteIdentifier(legacyConstraint.tableName())
            + " drop constraint if exists "
            + quoteIdentifier(legacyConstraint.foreignKeyName());
    }

    private String quoteIdentifier(final String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }

    private record LegacyConstraintRef(
        String tableName,
        String foreignKeyName
    ) {
    }
}
