package io.ssafy.p.j14c103.homerun.api.service.world.housing;

import io.ssafy.p.j14c103.homerun.api.service.world.housing.request.PreparedRealEstateImportServiceRequest;
import io.ssafy.p.j14c103.homerun.config.ConditionalOnRealEstateImportEnabled;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnRealEstateImportEnabled
@RequiredArgsConstructor
public class RealEstateImportPersistenceService {

    private final JdbcBatchRealEstateImportPersistenceService jdbcBatchRealEstateImportPersistenceService;
    private final JpaRealEstateImportPersistenceService jpaRealEstateImportPersistenceService;
    private final DataSource dataSource;
    private volatile Boolean postgresDatabase;

    public void persist(PreparedRealEstateImportServiceRequest preparedImport) {
        if (isPostgresDatabase()) {
            jdbcBatchRealEstateImportPersistenceService.persist(preparedImport);
            return;
        }
        jpaRealEstateImportPersistenceService.persist(preparedImport);
    }

    private boolean isPostgresDatabase() {
        Boolean cached = postgresDatabase;
        if (cached != null) {
            return cached;
        }

        synchronized (this) {
            if (postgresDatabase != null) {
                return postgresDatabase;
            }

            try (Connection connection = dataSource.getConnection()) {
                String productName = connection.getMetaData().getDatabaseProductName();
                postgresDatabase = productName != null
                    && productName.toLowerCase(Locale.ROOT).contains("postgresql");
                return postgresDatabase;
            } catch (SQLException exception) {
                throw new IllegalStateException("Failed to inspect datasource product name", exception);
            }
        }
    }
}
