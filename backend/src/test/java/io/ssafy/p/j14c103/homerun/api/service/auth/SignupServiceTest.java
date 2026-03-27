package io.ssafy.p.j14c103.homerun.api.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.auth.request.SignupServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.auth.response.SignupResponse;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountRepository;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialSummaryRepository;
import io.ssafy.p.j14c103.homerun.domain.paymenthistory.MemberPaymentHistoryRepository;
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
    private UserAccountRepository userAccountRepository;

    @Autowired
    private UserFinancialSummaryRepository userFinancialSummaryRepository;

    @Autowired
    private MemberPaymentHistoryRepository memberPaymentHistoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void tearDown() {
        memberPaymentHistoryRepository.deleteAllInBatch();
        userFinancialSummaryRepository.deleteAllInBatch();
        userAccountRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @DisplayName("회원가입에 성공하면 유저만 생성한다.")
    @Test
    void signup() {
        // given
        final SignupServiceRequest request = SignupServiceRequest.builder()
                .name("홍길동")
                .email("user@example.com")
                .password("Password123!")
                .paymentTypes(java.util.List.of("LIVING", "TRANSPORT", "TELECOM"))
                .build();

        // when
        final SignupResponse response = signupService.signup(request);

        // then
        final User savedUser = userRepository.findByEmail(Email.of("user@example.com"))
                .orElseThrow();

        assertThat(response.getUserId()).isNotNull();
        assertThat(response.getEmail()).isEqualTo("user@example.com");
        assertThat(response.getName()).isEqualTo("홍길동");
        assertThat(savedUser.getName()).isEqualTo("홍길동");
        assertThat(savedUser.getAuthProvider()).isEqualTo(AuthProvider.EMAIL);
        assertThat(savedUser.hasSsafyLink()).isFalse();
        assertThat(savedUser.getPaymentType()).isEqualTo("LIVING,TRANSPORT,TELECOM");
        assertThat(passwordEncoder.matches("Password123!", savedUser.getPasswordHash())).isTrue();
        assertThat(userAccountRepository.findByUserId(savedUser.getId())).isEmpty();
        assertThat(userFinancialSummaryRepository.findById(savedUser.getId())).isEmpty();
        assertThat(memberPaymentHistoryRepository.findAll()).isEmpty();
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
        final SignupServiceRequest request = SignupServiceRequest.builder()
                .name("홍길동")
                .email("user@example.com")
                .password("Password123!")
                .paymentTypes(java.util.List.of("LIVING"))
                .build();

        // when & then
        assertThatThrownBy(() -> signupService.signup(request))
                .isInstanceOf(HomerunException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_EMAIL_DUPLICATE);
    }
}
