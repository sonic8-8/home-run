package io.ssafy.p.j14c103.homerun.api.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import io.ssafy.p.j14c103.homerun.api.service.auth.request.SignupServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.auth.response.SignupResponse;
import io.ssafy.p.j14c103.homerun.domain.user.AuthProvider;
import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class SignupServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SignupService signupService;

    @DisplayName("회원가입에 성공하면 비밀번호를 해시해 사용자를 저장한다.")
    @Test
    void signup() {
        // given
        SignupServiceRequest request = SignupServiceRequest.builder()
                .name("홍길동")
                .email("user@example.com")
                .password("Password123!")
                .build();
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        given(userRepository.findByEmail(Email.of("user@example.com")))
                .willReturn(Optional.empty());
        given(passwordEncoder.encode("Password123!"))
                .willReturn("encoded-password");
        given(userRepository.save(any(User.class)))
                .willAnswer(invocation -> invocation.getArgument(0, User.class));

        // when
        SignupResponse response = signupService.signup(request);

        // then
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(response.getEmail()).isEqualTo("user@example.com");
        assertThat(response.getName()).isEqualTo("홍길동");
        assertThat(savedUser.getEmail()).isEqualTo(Email.of("user@example.com"));
        assertThat(savedUser.getName()).isEqualTo("홍길동");
        assertThat(savedUser.getPasswordHash()).isEqualTo("encoded-password");
        assertThat(savedUser.getPasswordHash()).isNotEqualTo("Password123!");
        assertThat(savedUser.getAuthProvider()).isEqualTo(AuthProvider.EMAIL);
    }

    @DisplayName("이미 가입된 이메일이면 예외가 발생한다.")
    @Test
    void signupWithDuplicateEmail() {
        // given
        SignupServiceRequest request = SignupServiceRequest.builder()
                .name("홍길동")
                .email("user@example.com")
                .password("Password123!")
                .build();
        given(userRepository.findByEmail(Email.of("user@example.com")))
                .willReturn(Optional.of(User.register(
                        Email.of("user@example.com"),
                        "기존 사용자",
                        "encoded-password"
                )));

        // when & then
        assertThatThrownBy(() -> signupService.signup(request))
                .isInstanceOf(HomerunException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_EMAIL_DUPLICATE);
    }
}
