package io.ssafy.p.j14c103.homerun.domain.gamesession.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.SessionStatus;
import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GameReportRepositoryTest {

    @Autowired
    private GameReportRepository gameReportRepository;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @DisplayName("game_reports는 achievements와 spending pattern을 포함해 조회된다")
    @Test
    void findGameReport() {
        final User user = userRepository.save(
            User.register(Email.of("game-report@example.com"), "tester", "hashed")
        );
        final GameSession gameSession = gameSessionRepository.saveAndFlush(GameSession.create(
            user.getId(),
            1,
            "윤서",
            CharacterType.FEMALE,
            JobType.STARTUP,
            HousingType.STUDIO,
            "11",
            "11680",
            450L,
            DataSourceType.PROFILE
        ));
        gameReportRepository.saveAndFlush(GameReport.create(
            gameSession.getGameSessionId(),
            SessionStatus.CLEAR,
            "부동산 갑부",
            120_000_000,
            80_000_000,
            "S",
            500_000_000,
            40_000_000,
            "식비",
            BigDecimal.valueOf(35.2),
            List.of(
                GameReportAchievement.of("첫 내집 마련", "/images/badges/first-house.png"),
                GameReportAchievement.of("주식 부자", "/images/badges/stock-rich.png")
            )
        ));

        final GameReport found = gameReportRepository.findById(gameSession.getGameSessionId())
            .orElseThrow();

        assertThat(found.getEndingType()).isEqualTo(SessionStatus.CLEAR);
        assertThat(found.getEndingTitle()).isEqualTo("부동산 갑부");
        assertThat(found.getTopSpendingCategory()).isEqualTo("식비");
        assertThat(found.getTopSpendingRatio()).isEqualByComparingTo(BigDecimal.valueOf(35.2));
        assertThat(found.getAchievements())
            .extracting(GameReportAchievement::getName, GameReportAchievement::getIconUrl)
            .containsExactly(
                tuple("첫 내집 마련", "/images/badges/first-house.png"),
                tuple("주식 부자", "/images/badges/stock-rich.png")
            );
    }
}
