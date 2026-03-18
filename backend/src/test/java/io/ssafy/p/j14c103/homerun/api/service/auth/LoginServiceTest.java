package io.ssafy.p.j14c103.homerun.api.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.auth.request.LoginServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.auth.response.LoginResponse;
import io.ssafy.p.j14c103.homerun.config.JwtProperties;
import io.ssafy.p.j14c103.homerun.config.JwtTokenProvider;
import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.domain.user.auth.RefreshToken;
import io.ssafy.p.j14c103.homerun.domain.user.auth.RefreshTokenRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@ActiveProfiles("test")
@Tag("container")
@Testcontainers(disabledWithoutDocker = true)
class LoginServiceTest {

    @Container
    private static final GenericContainer<?> REDIS_CONTAINER = new GenericContainer<>(
            DockerImageName.parse("redis:7.2-alpine")
    ).withExposedPorts(6379);

    @Autowired
    private LoginService loginService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @DynamicPropertySource
    static void overrideRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", REDIS_CONTAINER::getFirstMappedPort);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAllInBatch();

        Set<String> keys = stringRedisTemplate.keys("auth:refresh:*");

        if (keys == null || keys.isEmpty()) {
            return;
        }

        stringRedisTemplate.delete(keys);
    }

    @DisplayName("로그인에 성공하면 Access Token, Refresh Token을 발급하고 Refresh Token을 저장한다.")
    @Test
    void login() {
        // given
        User savedUser = userRepository.saveAndFlush(User.register(
                Email.of("user@example.com"),
                "홍길동",
                passwordEncoder.encode("Password123!")
        ));
        LoginServiceRequest request = LoginServiceRequest.builder()
                .email("user@example.com")
                .password("Password123!")
                .build();

        // when
        LoginResponse response = loginService.login(request);

        // then
        RefreshToken refreshToken = refreshTokenRepository.findByUserId(savedUser.getId())
                .orElseThrow();
        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getRefreshToken()).isNotBlank();
        assertThat(response.getAccessTokenExpiresIn()).isEqualTo(jwtProperties.getAccessTokenTtlSeconds());
        assertThat(jwtTokenProvider.getUserId(response.getAccessToken())).isEqualTo(savedUser.getId());
        assertThat(jwtTokenProvider.getEmail(response.getAccessToken())).isEqualTo("user@example.com");
        assertThat(jwtTokenProvider.getTokenType(response.getAccessToken())).isEqualTo("access");
        assertThat(jwtTokenProvider.getRefreshTokenUserId(response.getRefreshToken())).isEqualTo(savedUser.getId());
        assertThat(jwtTokenProvider.getRefreshTokenEmail(response.getRefreshToken())).isEqualTo("user@example.com");
        assertThat(refreshToken.getUserId()).isEqualTo(savedUser.getId());
        assertThat(refreshToken.getTokenValue()).isEqualTo(response.getRefreshToken());
        assertThat(refreshToken.getExpiresAt()).isEqualTo(jwtTokenProvider.getExpiresAt(response.getRefreshToken()));
    }

    @DisplayName("비밀번호가 일치하지 않으면 로그인 실패 예외가 발생한다.")
    @Test
    void loginWithInvalidPassword() {
        // given
        User savedUser = userRepository.saveAndFlush(User.register(
                Email.of("user@example.com"),
                "홍길동",
                passwordEncoder.encode("Password123!")
        ));
        LoginServiceRequest request = LoginServiceRequest.builder()
                .email("user@example.com")
                .password("Password999!")
                .build();

        // when & then
        assertThatThrownBy(() -> loginService.login(request))
                .isInstanceOf(HomerunException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_LOGIN_FAILED);
        assertThat(refreshTokenRepository.findByUserId(savedUser.getId())).isEmpty();
    }

    @DisplayName("가입되지 않은 이메일이면 로그인 실패 예외가 발생한다.")
    @Test
    void loginWithUnknownEmail() {
        // given
        LoginServiceRequest request = LoginServiceRequest.builder()
                .email("user@example.com")
                .password("Password123!")
                .build();

        // when & then
        assertThatThrownBy(() -> loginService.login(request))
                .isInstanceOf(HomerunException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_LOGIN_FAILED);
    }
}
