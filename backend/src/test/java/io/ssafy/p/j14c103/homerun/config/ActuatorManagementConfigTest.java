package io.ssafy.p.j14c103.homerun.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.support.ActuatorManagementTestSupport;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.core.env.Environment;

class ActuatorManagementConfigTest extends ActuatorManagementTestSupport {

    @Autowired
    private Environment environment;

    @Autowired
    private WebEndpointsSupplier webEndpointsSupplier;

    @Autowired
    private TestRestTemplate testRestTemplate;

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
        assertThat(environment.getProperty(
                "management.metrics.distribution.percentiles-histogram.http.server.requests",
                Boolean.class
        )).isTrue();
    }

    @DisplayName("prod 프로필에서 health probes를 활성화한다")
    @Test
    void enablesHealthProbes() {
        assertThat(environment.getProperty(
                "management.endpoint.health.probes.enabled",
                Boolean.class
        )).isTrue();
    }

    @DisplayName("prod 프로필에서 HTTP 요청 histogram bucket을 prometheus endpoint로 노출한다")
    @Test
    void exposesHttpServerRequestHistogramBuckets() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        testRestTemplate.postForEntity(
                "/api/auth/login",
                new HttpEntity<>("{\"email\":\"invalid\",\"password\":\"invalid\"}", headers),
                String.class
        );

        String prometheusMetrics = testRestTemplate.getForObject("/actuator/prometheus", String.class);

        assertThat(prometheusMetrics).contains("http_server_requests_seconds_bucket");
        assertThat(prometheusMetrics).contains("uri=\"/api/auth/login\"");
    }
}
