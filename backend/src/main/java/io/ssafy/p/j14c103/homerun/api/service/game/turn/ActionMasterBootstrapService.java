package io.ssafy.p.j14c103.homerun.api.service.game.turn;

import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.ActionCatalog;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.ActionCatalog.ActionDefinition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActionMasterBootstrapService implements ApplicationRunner {

    private static final String CREATE_ACTION_MASTER_TABLE_SQL = """
        create table if not exists action_masters (
          action_type varchar(30) primary key,
          action_category varchar(20) not null,
          action_name varchar(100) not null,
          description text,
          active_yn boolean not null default true
        )
        """;

    private static final String UPDATE_ACTION_MASTER_SQL = """
        update action_masters
           set action_category = ?,
               action_name = ?,
               description = ?,
               active_yn = ?
         where action_type = ?
        """;

    private static final String INSERT_ACTION_MASTER_SQL = """
        insert into action_masters (
            action_type,
            action_category,
            action_name,
            description,
            active_yn
        ) values (?, ?, ?, ?, ?)
        """;

    private final JdbcTemplate jdbcTemplate;
    private final ActionCatalog actionCatalog;

    @Override
    @Transactional
    public void run(final ApplicationArguments args) {
        synchronizeCatalog();
        log.info("Action master bootstrap completed. count={}", actionCatalog.getDefinitions().size());
    }

    @Transactional
    public void synchronizeCatalog() {
        jdbcTemplate.execute(CREATE_ACTION_MASTER_TABLE_SQL);

        for (final ActionDefinition definition : actionCatalog.getDefinitions()) {
            upsert(definition);
        }
    }

    private void upsert(final ActionDefinition definition) {
        final int updated = jdbcTemplate.update(
            UPDATE_ACTION_MASTER_SQL,
            definition.category().name(),
            definition.label(),
            null,
            true,
            definition.actionType().name()
        );

        if (updated > 0) {
            return;
        }

        jdbcTemplate.update(
            INSERT_ACTION_MASTER_SQL,
            definition.actionType().name(),
            definition.category().name(),
            definition.label(),
            null,
            true
        );
    }
}
