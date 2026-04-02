package io.ssafy.p.j14c103.homerun.api.service.game.turn;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LegacyActionMasterConstraintCompatibilityServiceTest {

    @DisplayName("legacy action_masters FK가 없으면 startup 호환 정리를 건너뛴다.")
    @Test
    void ensureCompatibleSchemaSkipsWithoutLegacyConstraint() throws Exception {
        // given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        final ResultSet lowerCaseImportedKeys = mock(ResultSet.class);
        final ResultSet upperCaseImportedKeys = mock(ResultSet.class);
        final LegacyActionMasterConstraintCompatibilityService compatibilityService =
            new LegacyActionMasterConstraintCompatibilityService(dataSource);
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.getMetaData()).willReturn(metaData);
        given(metaData.getImportedKeys(null, null, "game_turn_slots")).willReturn(lowerCaseImportedKeys);
        given(metaData.getImportedKeys(null, null, "GAME_TURN_SLOTS")).willReturn(upperCaseImportedKeys);
        given(lowerCaseImportedKeys.next()).willReturn(false);
        given(upperCaseImportedKeys.next()).willReturn(false);

        // when // then
        assertThatCode(compatibilityService::ensureCompatibleSchema).doesNotThrowAnyException();
        verify(connection, never()).createStatement();
    }

    @DisplayName("legacy action_masters FK가 있으면 startup 전에 drop constraint로 정리한다.")
    @Test
    void ensureCompatibleSchemaDropsLegacyConstraint() throws Exception {
        // given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        final ResultSet lowerCaseImportedKeys = mock(ResultSet.class);
        final ResultSet upperCaseImportedKeys = mock(ResultSet.class);
        final Statement statement = mock(Statement.class);
        final LegacyActionMasterConstraintCompatibilityService compatibilityService =
            new LegacyActionMasterConstraintCompatibilityService(dataSource);
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.getMetaData()).willReturn(metaData);
        given(metaData.getImportedKeys(null, null, "game_turn_slots")).willReturn(lowerCaseImportedKeys);
        given(metaData.getImportedKeys(null, null, "GAME_TURN_SLOTS")).willReturn(upperCaseImportedKeys);
        given(lowerCaseImportedKeys.next()).willReturn(true, false);
        given(upperCaseImportedKeys.next()).willReturn(false);
        given(lowerCaseImportedKeys.getString("FK_NAME")).willReturn("fk_game_turn_slots__action_master");
        given(lowerCaseImportedKeys.getString("PKTABLE_NAME")).willReturn("action_masters");
        given(lowerCaseImportedKeys.getString("FKTABLE_NAME")).willReturn("game_turn_slots");
        given(connection.createStatement()).willReturn(statement);

        // when // then
        assertThatCode(compatibilityService::ensureCompatibleSchema).doesNotThrowAnyException();
        verify(statement).execute(
            "alter table if exists \"game_turn_slots\" drop constraint if exists \"fk_game_turn_slots__action_master\""
        );
    }

    @DisplayName("legacy action_masters FK 정리 중 SQL 예외가 나면 명확한 예외로 감싼다.")
    @Test
    void ensureCompatibleSchemaFailsWhenCleanupThrowsSQLException() throws Exception {
        // given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        final ResultSet lowerCaseImportedKeys = mock(ResultSet.class);
        final ResultSet upperCaseImportedKeys = mock(ResultSet.class);
        final Statement statement = mock(Statement.class);
        final LegacyActionMasterConstraintCompatibilityService compatibilityService =
            new LegacyActionMasterConstraintCompatibilityService(dataSource);
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.getMetaData()).willReturn(metaData);
        given(metaData.getImportedKeys(null, null, "game_turn_slots")).willReturn(lowerCaseImportedKeys);
        given(metaData.getImportedKeys(null, null, "GAME_TURN_SLOTS")).willReturn(upperCaseImportedKeys);
        given(lowerCaseImportedKeys.next()).willReturn(true, false);
        given(upperCaseImportedKeys.next()).willReturn(false);
        given(lowerCaseImportedKeys.getString("FK_NAME")).willReturn("fk_game_turn_slots__action_master");
        given(lowerCaseImportedKeys.getString("PKTABLE_NAME")).willReturn("action_masters");
        given(lowerCaseImportedKeys.getString("FKTABLE_NAME")).willReturn("game_turn_slots");
        given(connection.createStatement()).willReturn(statement);
        given(statement.execute(
            "alter table if exists \"game_turn_slots\" drop constraint if exists \"fk_game_turn_slots__action_master\""
        )).willThrow(new SQLException("drop failed"));

        // when // then
        assertThatThrownBy(compatibilityService::ensureCompatibleSchema)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Failed to clean up legacy action_masters constraints.");
    }
}
