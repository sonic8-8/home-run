package io.ssafy.p.j14c103.homerun.domain.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @DisplayName("이메일로 사용자를 찾을 수 있다.")
    @Test
    void findByEmail() {
        // given
        Email email = Email.of("test@ssafy.com");
        User user = User.create(
            email,
            "tester",
            "hashed-password",
            null
        );

        userRepository.save(user);

        // when
        User result = userRepository.findByEmail(Email.of("test@ssafy.com"))
            .orElseThrow();

        // then
        assertThat(result.getEmail()).isEqualTo(Email.of("test@ssafy.com"));
    }
}
