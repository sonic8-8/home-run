package io.ssafy.p.j14c103.homerun.domain.user;

import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @DisplayName("이메일로 사용자를 찾을 수 있다.")
    @Test
    void findByEmail() {
        // given
        Email email = Email.of("test@ssafy.com");
        User user = User.register(
                email,
                "tester",
                "hashed-password"
        );

        userRepository.save(user);

        // when
        User result = userRepository.findByEmail(Email.of("test@ssafy.com"))
                .orElseThrow();

        // then
        assertThat(result.getEmail()).isEqualTo(Email.of("test@ssafy.com"));
    }

    @DisplayName("이메일은 유일해야 한다.")
    @Test
    void emailShouldBeUnique() {
        // given
        User user = User.register(
                Email.of("duplicate@ssafy.com"),
                "tester",
                "hashed-password"
        );

        userRepository.saveAndFlush(
                user
        );

        User duplicateUser = User.register(
                Email.of("duplicate@ssafy.com"),
                "tester-2",
                "hashed-password-2"
        );

        // when & then
        assertThatThrownBy(() -> userRepository.saveAndFlush(duplicateUser))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @DisplayName("SSAFY 사용자 키는 유일해야 한다.")
    @Test
    void ssafyUserKeyShouldBeUnique() {
        // given
        User firstUser = User.register(
                Email.of("first@ssafy.com"),
                "tester-1",
                "hashed-password-1"
        );
        firstUser.linkSsafy(
                "linked-ssafy-user-key",
                LocalDateTime.of(2026, 3, 16, 12, 0)
        );
        userRepository.saveAndFlush(firstUser);

        User secondUser = User.register(
                Email.of("second@ssafy.com"),
                "tester-2",
                "hashed-password-2"
        );
        secondUser.linkSsafy(
                "linked-ssafy-user-key",
                LocalDateTime.of(2026, 3, 16, 12, 30)
        );

        // when & then
        assertThatThrownBy(() -> userRepository.saveAndFlush(secondUser))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @DisplayName("회원 확장 필드를 저장하고 조회할 수 있다.")
    @Test
    void saveAndLoadNewPersistenceFields() {
        // given
        LocalDateTime connectedAt = LocalDateTime.of(2026, 3, 16, 10, 30);
        LocalDateTime verifiedAt = LocalDateTime.of(2026, 3, 16, 11, 0);
        User user = User.register(
                Email.of("extended@ssafy.com"),
                "tester",
                "hashed-password"
        );
        user.changeNickname("홈런");
        user.linkSsafy("ssafy-user-key", connectedAt);
        user.markAccountVerified(verifiedAt);

        userRepository.saveAndFlush(user);
        entityManager.clear();

        // when
        User result = userRepository.findByEmail(Email.of("extended@ssafy.com"))
                .orElseThrow();

        // then
        assertThat(result.getNickname()).isEqualTo("홈런");
        assertThat(result.getAuthProvider()).isEqualTo(AuthProvider.EMAIL);
        assertThat(result.getSsafyUserKey()).isEqualTo("ssafy-user-key");
        assertThat(result.getSsafyConnectedAt()).isEqualTo(connectedAt);
        assertThat(result.getAccountAuthVerifiedAt()).isEqualTo(verifiedAt);
        assertThat(result.getCreatedAt()).isNotNull();
    }
}
