package io.ssafy.p.j14c103.homerun.api.service.game.turn;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.jpa.hibernate.ddl-auto=update",
        "spring.sql.init.mode=never",
        "app.real-estate-import.enabled=false",
        "management.endpoints.web.exposure.include=health,prometheus",
        "management.endpoint.health.probes.enabled=true"
    }
)
class LegacyActionMasterRuntimeCompatibilityTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer<>(
        DockerImageName.parse("postgres:16-alpine")
    )
        .withDatabaseName("compat_test")
        .withUsername("compat_user")
        .withPassword("compat_password")
        .withCopyFileToContainer(
            MountableFile.forClasspathResource("sql/legacy-action-master-runtime-fk.sql"),
            "/docker-entrypoint-initdb.d/001-legacy-action-master-runtime-fk.sql"
        )
        .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*\\n", 1))
        .withStartupTimeout(Duration.ofMinutes(2));

    @Autowired
    private TestRestTemplate testRestTemplate;

    @DisplayName("legacy action_masters FK 잔재가 있어도 런타임 기동과 health 계약을 유지한다")
    @Test
    void runtimeStartsWithLegacyActionMasterForeignKeyResidue() {
        // when
        final ResponseEntity<String> liveness = testRestTemplate.getForEntity(
            "/actuator/health/liveness",
            String.class
        );
        final ResponseEntity<String> readiness = testRestTemplate.getForEntity(
            "/actuator/health/readiness",
            String.class
        );

        // then
        assertThat(liveness.getStatusCode().is2xxSuccessful())
            .as("liveness body=%s", liveness.getBody())
            .isTrue();
        assertThat(readiness.getStatusCode().is2xxSuccessful())
            .as("readiness body=%s", readiness.getBody())
            .isTrue();
    }
}
