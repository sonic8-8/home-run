package io.ssafy.p.j14c103.homerun.api.service.game.turn;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LegacyActionMasterConstraintValidationServiceTest {

    @DisplayName("legacy action_masters FK가 없으면 부팅 검증을 통과한다.")
    @Test
    void validatePassesWithoutLegacyConstraint() throws Exception {
        // given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        final ResultSet lowerCaseImportedKeys = mock(ResultSet.class);
        final ResultSet upperCaseImportedKeys = mock(ResultSet.class);
        final LegacyActionMasterConstraintValidationService validator =
            new LegacyActionMasterConstraintValidationService(dataSource);
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.getMetaData()).willReturn(metaData);
        given(metaData.getImportedKeys(null, null, "game_turn_slots")).willReturn(lowerCaseImportedKeys);
        given(metaData.getImportedKeys(null, null, "GAME_TURN_SLOTS")).willReturn(upperCaseImportedKeys);
        given(lowerCaseImportedKeys.next()).willReturn(false);
        given(upperCaseImportedKeys.next()).willReturn(false);

        // when // then
        assertThatCode(validator::validate).doesNotThrowAnyException();
    }

    @DisplayName("legacy action_masters FK가 있으면 명확한 예외로 부팅을 막는다.")
    @Test
    void validateFailsWithLegacyConstraint() throws Exception {
        // given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        final ResultSet importedKeys = mock(ResultSet.class);
        final LegacyActionMasterConstraintValidationService validator =
            new LegacyActionMasterConstraintValidationService(dataSource);
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.getMetaData()).willReturn(metaData);
        given(metaData.getImportedKeys(null, null, "game_turn_slots")).willReturn(importedKeys);
        given(importedKeys.next()).willReturn(true, false);
        given(importedKeys.getString("FK_NAME")).willReturn("fk_game_turn_slots__action_master");
        given(importedKeys.getString("PKTABLE_NAME")).willReturn("action_masters");

        // when // then
        assertThatThrownBy(validator::validate)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("fk_game_turn_slots__action_master")
            .hasMessageContaining("Remove the constraint before starting the application");
    }
}
