package io.ssafy.p.j14c103.homerun.api.service.game.stock;

import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockMarketBootstrapService implements ApplicationRunner {

    private static final String STOCK_MARKET_COUNT_SQL = "select count(*) from stock_markets";
    private static final String SNAPSHOT_SQL_PATH = "sql/stock_market_snapshot_validation.sql";

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    @Override
    @Transactional
    public void run(final ApplicationArguments args) throws Exception {
        bootstrapIfEmpty();
    }

    @Transactional
    public void bootstrapIfEmpty() throws Exception {
        final Integer existingCount = jdbcTemplate.queryForObject(STOCK_MARKET_COUNT_SQL, Integer.class);

        if (existingCount != null && existingCount > 0) {
            log.info("Stock market bootstrap skipped. count={}", existingCount);
            return;
        }

        ScriptUtils.executeSqlScript(dataSource.getConnection(), new ClassPathResource(SNAPSHOT_SQL_PATH));

        final Integer seededCount = jdbcTemplate.queryForObject(STOCK_MARKET_COUNT_SQL, Integer.class);
        log.info("Stock market bootstrap completed. count={}", seededCount);
    }
}
