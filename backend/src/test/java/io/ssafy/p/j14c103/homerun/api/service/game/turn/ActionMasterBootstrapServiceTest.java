package io.ssafy.p.j14c103.homerun.api.service.game.turn;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.ActionCatalog;
import io.ssafy.p.j14c103.homerun.support.IntegrationTestSupport;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class ActionMasterBootstrapServiceTest extends IntegrationTestSupport {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ActionCatalog actionCatalog;

    @DisplayName("애플리케이션 부팅 시 action_masters가 생성되고 기본 액션이 적재된다.")
    @Test
    void bootstrapActionMasters() {
        // when
        final Integer actionCount = jdbcTemplate.queryForObject(
            "select count(*) from action_masters",
            Integer.class
        );
        final Map<String, Object> restRow = jdbcTemplate.queryForMap(
            "select action_type, action_category, action_name, active_yn from action_masters where action_type = ?",
            "REST"
        );

        // then
        assertThat(actionCount).isEqualTo(actionCatalog.getDefinitions().size());
        assertThat(restRow)
            .containsEntry("action_type", "REST")
            .containsEntry("action_category", "ACTIVITY")
            .containsEntry("action_name", "휴식")
            .containsEntry("active_yn", true);
    }
}
