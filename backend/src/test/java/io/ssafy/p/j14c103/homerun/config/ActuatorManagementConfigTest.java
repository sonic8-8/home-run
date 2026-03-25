package io.ssafy.p.j14c103.homerun.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:actuator-management-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never",
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379",
        "jwt.secret=01234567890123456789012345678901",
        "jwt.access-token-ttl-seconds=1800",
        "jwt.refresh-token-ttl-seconds=1209600",
        "app.real-estate-import.enabled=false"
})
@AutoConfigureObservability
@ActiveProfiles("prod")
class ActuatorManagementConfigTest {

    @Autowired
    private Environment environment;

    @Autowired
    private WebEndpointsSupplier webEndpointsSupplier;

    @DisplayName("prod 프로필에서 health와 prometheus actuator endpoint를 노출한다")
    @Test
    void exposesHealthAndPrometheusEndpoints() {
        Set<String> endpointIds = webEndpointsSupplier.getEndpoints().stream()
                .map(endpoint -> endpoint.getEndpointId().toString())
                .collect(java.util.stream.Collectors.toSet());

        assertThat(endpointIds).contains("health", "prometheus");
        assertThat(environment.getProperty("management.endpoints.web.exposure.include"))
                .isEqualTo("health,prometheus");
        assertThat(environment.getProperty("management.metrics.tags.application"))
                .isEqualTo("homerun");
    }

    @DisplayName("prod 프로필에서 health probes를 활성화한다")
    @Test
    void enablesHealthProbes() {
        assertThat(environment.getProperty(
                "management.endpoint.health.probes.enabled",
                Boolean.class
        )).isTrue();
    }
}
