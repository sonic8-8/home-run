package io.ssafy.p.j14c103.homerun.api.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.auth.request.SignupServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.auth.response.SignupResponse;
import io.ssafy.p.j14c103.homerun.domain.user.AuthProvider;
import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SignupServiceTest {

    @Autowired
    private SignupService signupService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void tearDown() {
        userRepository.deleteAllInBatch();
    }

    @DisplayName("회원가입에 성공하면 비밀번호를 해시해 사용자를 저장한다.")
    @Test
    void signup() {
        // given
        SignupServiceRequest request = SignupServiceRequest.builder()
                .name("홍길동")
                .email("user@example.com")
                .password("Password123!")
                .build();

        // when
        SignupResponse response = signupService.signup(request);

        // then
        User savedUser = userRepository.findByEmail(Email.of("user@example.com"))
                .orElseThrow();
        assertThat(response.getUserId()).isNotNull();
        assertThat(response.getEmail()).isEqualTo("user@example.com");
        assertThat(response.getName()).isEqualTo("홍길동");
        assertThat(savedUser.getEmail()).isEqualTo(Email.of("user@example.com"));
        assertThat(savedUser.getName()).isEqualTo("홍길동");
        assertThat(savedUser.getPasswordHash()).isNotEqualTo("Password123!");
        assertThat(passwordEncoder.matches("Password123!", savedUser.getPasswordHash())).isTrue();
        assertThat(savedUser.getAuthProvider()).isEqualTo(AuthProvider.EMAIL);
    }

    @DisplayName("이미 가입된 이메일이면 예외가 발생한다.")
    @Test
    void signupWithDuplicateEmail() {
        // given
        userRepository.saveAndFlush(User.register(
                Email.of("user@example.com"),
                "기존 사용자",
                passwordEncoder.encode("Password123!")
        ));
        SignupServiceRequest request = SignupServiceRequest.builder()
                .name("홍길동")
                .email("user@example.com")
                .password("Password123!")
                .build();

        // when & then
        assertThatThrownBy(() -> signupService.signup(request))
                .isInstanceOf(HomerunException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_EMAIL_DUPLICATE);
    }
}
