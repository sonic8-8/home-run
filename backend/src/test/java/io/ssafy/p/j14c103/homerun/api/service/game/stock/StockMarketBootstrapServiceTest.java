package io.ssafy.p.j14c103.homerun.api.service.game.stock;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class StockMarketBootstrapServiceTest {

    private JdbcTemplate jdbcTemplate;
    private StockMarketBootstrapService stockMarketBootstrapService;

    @BeforeEach
    void setUp() {
        final DataSource dataSource = createDataSource();
        jdbcTemplate = new JdbcTemplate(dataSource);
        stockMarketBootstrapService = new StockMarketBootstrapService(jdbcTemplate, dataSource);

        jdbcTemplate.execute("drop table if exists stock_markets");
        jdbcTemplate.execute("""
            create table stock_markets (
              stock_code varchar(20) primary key,
              stock_name varchar(100) not null,
              kis_stock_code varchar(10),
              sector varchar(100),
              base_price_amount integer,
              year_low_price_amount integer,
              year_high_price_amount integer,
              volatility_rate numeric(8, 4)
            )
            """);
    }

    @DisplayName("stock_markets가 비어 있으면 검증된 스냅샷 SQL로 종목 마스터를 적재한다")
    @Test
    void bootstrapIfEmpty_seedsSnapshotRows() throws Exception {
        stockMarketBootstrapService.bootstrapIfEmpty();

        final Integer count = jdbcTemplate.queryForObject("select count(*) from stock_markets", Integer.class);

        assertThat(count).isEqualTo(50);
        assertThat(jdbcTemplate.queryForObject(
            "select stock_name from stock_markets where stock_code = '005930'",
            String.class
        )).isEqualTo("삼성전자");
    }

    @DisplayName("stock_markets에 기존 데이터가 있으면 추가 적재를 건너뛴다")
    @Test
    void bootstrapIfEmpty_skipsWhenAlreadySeeded() throws Exception {
        jdbcTemplate.update("""
            insert into stock_markets (
                stock_code,
                stock_name,
                kis_stock_code,
                sector,
                base_price_amount
            ) values (?, ?, ?, ?, ?)
            """,
            "TEST",
            "테스트 종목",
            "000000",
            "테스트",
            1000
        );

        stockMarketBootstrapService.bootstrapIfEmpty();

        final Integer count = jdbcTemplate.queryForObject("select count(*) from stock_markets", Integer.class);

        assertThat(count).isEqualTo(1);
    }

    private DataSource createDataSource() {
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:stock-market-bootstrap;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }
}
