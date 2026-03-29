package io.ssafy.p.j14c103.homerun.support;

import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
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
    }
)
@AutoConfigureObservability
@ActiveProfiles("prod")
public abstract class ActuatorManagementTestSupport {
}
