package io.ssafy.p.j14c103.homerun.support;

import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.test.context.TestPropertySource;

@AutoConfigureObservability
@TestPropertySource(properties = {
    "management.endpoints.web.exposure.include=health,prometheus",
    "management.endpoint.health.probes.enabled=true",
    "management.health.redis.enabled=false"
})
public abstract class SecurityConfigTestSupport extends HttpIntegrationTestSupport {
}
