package io.ssafy.p.j14c103.homerun.api.service.home.credit;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.api.service.user.UserAuthContextService;
import io.ssafy.p.j14c103.homerun.client.ssafy.SsafyCreditCardClient;
import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class FicoCreditScoringServiceTest {

    @Autowired
    private FicoCreditScoringService ficoCreditScoringService;

    @MockitoBean
    private SsafyCreditCardClient creditCardClient;

    @Autowired
    private UserAuthContextService userAuthContextService;

    @Autowired
    private UserRepository userRepository;

    private Long userId;

    @BeforeEach
    void setUp() {
        final User user = User.register(Email.of("test@test.com"), "tester", "password");
        userRepository.save(user);
        this.userId = user.getId();
    }

    @Test
    @DisplayName("금융 이력 없는 사용자는 보수적 기본 점수를 받는다")
    void 이력없는사용자_보수적기본점수() {
        // when
        final CreditScore result = ficoCreditScoringService.calculate(userId);

        // then
        assertThat(result.getPaymentHistory()).isEqualTo(175);
        assertThat(result.getAmountsOwed()).isEqualTo(200);
        assertThat(result.getCreditMix()).isEqualTo(20);
        assertThat(result.getNewCredit()).isEqualTo(100);
        assertThat(result.getScore()).isLessThan(600);
        assertThat(result.getGrade()).isEqualTo(5);
        assertThat(result.getGradeLabel()).isEqualTo("Poor");
    }

    @Test
    @DisplayName("CSS 총점은 5요소 합산이며 1000점을 초과하지 않는다")
    void CSS총점_5요소합산_최대1000() {
        // when
        final CreditScore result = ficoCreditScoringService.calculate(userId);

        // then
        final int expectedTotal = result.getPaymentHistory() + result.getAmountsOwed()
                + result.getCreditLength() + result.getCreditMix() + result.getNewCredit();
        assertThat(result.getScore()).isEqualTo(expectedTotal);
        assertThat(result.getScore()).isLessThanOrEqualTo(1000);
    }

    @Test
    @DisplayName("등급별 금리 계수는 1등급 0.0, 5등급 1.0이다")
    void 등급별_금리계수() {
        // given & when & then
        assertThat(CreditScore.of(350, 300, 150, 100, 100).rateCoefficient()).isEqualTo(0.0);
        assertThat(CreditScore.of(300, 250, 120, 80, 80).rateCoefficient()).isEqualTo(0.25);
        assertThat(CreditScore.of(250, 200, 100, 70, 80).rateCoefficient()).isEqualTo(0.50);
        assertThat(CreditScore.of(200, 150, 80, 50, 50).rateCoefficient()).isEqualTo(1.0);
    }
}
