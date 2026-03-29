package io.ssafy.p.j14c103.homerun.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

public abstract class RedisContainerTestSupport extends IntegrationTestSupport {

  @Container
  protected static final GenericContainer<?> REDIS_CONTAINER = new GenericContainer<>(
      DockerImageName.parse("redis:7.2-alpine")
  ).withExposedPorts(6379);

  @DynamicPropertySource
  static void overrideRedisProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
    registry.add("spring.data.redis.port", REDIS_CONTAINER::getFirstMappedPort);
  }
}
