package io.ssafy.p.j14c103.homerun.api.service.game.turn;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class LegacyActionMasterConstraintValidationService implements ApplicationRunner {

    private static final String GAME_TURN_SLOTS_TABLE = "game_turn_slots";
    private static final String LEGACY_CONSTRAINT = "fk_game_turn_slots__action_master";
    private static final String ACTION_MASTERS_TABLE = "action_masters";

    private final DataSource dataSource;

    @Override
    public void run(final ApplicationArguments args) {
        validate();
    }

    void validate() {
        try (Connection connection = dataSource.getConnection()) {
            final DatabaseMetaData metaData = connection.getMetaData();

            if (!hasLegacyConstraint(metaData)) {
                return;
            }

            throw new IllegalStateException(
                "Legacy action_masters foreign key detected: fk_game_turn_slots__action_master. "
                    + "Remove the constraint before starting the application."
            );
        } catch (final SQLException exception) {
            throw new IllegalStateException("Failed to validate legacy action_masters constraints.", exception);
        }
    }

    private boolean hasLegacyConstraint(final DatabaseMetaData metaData) throws SQLException {
        return hasLegacyConstraint(metaData, GAME_TURN_SLOTS_TABLE)
            || hasLegacyConstraint(metaData, GAME_TURN_SLOTS_TABLE.toUpperCase());
    }

    private boolean hasLegacyConstraint(
        final DatabaseMetaData metaData,
        final String gameTurnSlotsTable
    ) throws SQLException {
        try (ResultSet importedKeys = metaData.getImportedKeys(null, null, gameTurnSlotsTable)) {
            while (importedKeys.next()) {
                if (isLegacyActionMasterConstraint(importedKeys)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isLegacyActionMasterConstraint(final ResultSet importedKeys) throws SQLException {
        final String foreignKeyName = importedKeys.getString("FK_NAME");
        final String parentTableName = importedKeys.getString("PKTABLE_NAME");

        if (LEGACY_CONSTRAINT.equalsIgnoreCase(foreignKeyName)) {
            return true;
        }

        return ACTION_MASTERS_TABLE.equalsIgnoreCase(parentTableName);
    }
}
