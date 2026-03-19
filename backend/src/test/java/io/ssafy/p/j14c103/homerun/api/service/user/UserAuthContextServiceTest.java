package io.ssafy.p.j14c103.homerun.api.service.user;

import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserAuthContextServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserAuthContextService userAuthContextService;

  @DisplayName("사용자 ID로 인증 컨텍스트를 조회한다")
  @Test
  void getContext() {
    // given
    final User user = createUser(1L, "ssafy-key");
    given(userRepository.findById(1L)).willReturn(Optional.of(user));

    // when
    final UserAuthContext context = userAuthContextService.getContext(1L);

    // then
    assertThat(context.userId()).isEqualTo(1L);
    assertThat(context.ssafyUserKey()).isEqualTo("ssafy-key");
  }

  @DisplayName("SSAFY 연동 키가 있으면 반환한다")
  @Test
  void getRequiredSsafyUserKey() {
    // given
    final User user = createUser(1L, "ssafy-key");
    given(userRepository.findById(1L)).willReturn(Optional.of(user));

    // when
    final String userKey = userAuthContextService.getRequiredSsafyUserKey(1L);

    // then
    assertThat(userKey).isEqualTo("ssafy-key");
  }

  @DisplayName("존재하지 않는 사용자면 예외가 발생한다")
  @Test
  void getContext_userNotFound_exception() {
    // given
    given(userRepository.findById(1L)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> userAuthContextService.getContext(1L))
            .isInstanceOf(HomerunException.class);
  }

  @DisplayName("SSAFY 연동 키가 없으면 예외가 발생한다")
  @Test
  void getRequiredSsafyUserKey_missingLink_exception() {
    // given
    final User user = createUser(1L, null);
    given(userRepository.findById(1L)).willReturn(Optional.of(user));

    // when & then
    assertThatThrownBy(() -> userAuthContextService.getRequiredSsafyUserKey(1L))
            .isInstanceOf(HomerunException.class);
  }

    private User createUser(final Long userId, final String ssafyUserKey) {
        final User user = User.register(
                Email.of("user@example.com"),
                "tester",
                "hashed-password"
        );
        ReflectionTestUtils.setField(user, "id", userId);
        if (ssafyUserKey == null) {
            return user;
        }

        user.linkSsafy(ssafyUserKey, LocalDateTime.of(2026, 3, 18, 12, 0));
        return user;
    }
}
