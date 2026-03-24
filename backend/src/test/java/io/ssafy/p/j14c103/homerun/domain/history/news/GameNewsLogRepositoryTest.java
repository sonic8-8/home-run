package io.ssafy.p.j14c103.homerun.domain.history.news;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.domain.world.news.NewsMaster;
import io.ssafy.p.j14c103.homerun.domain.world.news.NewsMasterRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GameNewsLogRepositoryTest {

    @Autowired
    private NewsMasterRepository newsMasterRepository;

    @Autowired
    private GameNewsLogRepository gameNewsLogRepository;

    @Autowired
    private EntityManager entityManager;

    @DisplayName("GameNewsLog는 세션 기준 최근 턴 순으로 지난 뉴스 이력을 조회할 수 있다")
    @Test
    void findNewsHistoryByGameSessionIdOrderByTurnNumberDesc() {
        // given
        NewsMaster firstNews = newsMasterRepository.saveAndFlush(
            NewsMaster.create(
                "NEWS-001",
                "금리 인하 기조 지속",
                "ECONOMY",
                "POSITIVE",
                Map.of("FINANCE", 2),
                0,
                3,
                Map.of("STARTUP", Map.of("salaryMult", 95))
            )
        );
        NewsMaster secondNews = newsMasterRepository.saveAndFlush(
            NewsMaster.create(
                "NEWS-002",
                "채용 한파 심화",
                "JOB",
                "NEGATIVE",
                Map.of("FINANCE", -1),
                0,
                -1,
                Map.of("STARTUP", Map.of("salaryMult", 90))
            )
        );

        gameNewsLogRepository.saveAndFlush(
            GameNewsLog.create(
                1001L,
                9,
                firstNews.getNewsId(),
                "금리 인하 기조 지속",
                LocalDate.of(2026, 9, 1)
            )
        );
        gameNewsLogRepository.saveAndFlush(
            GameNewsLog.create(
                1001L,
                10,
                secondNews.getNewsId(),
                "채용 한파 심화",
                LocalDate.of(2026, 10, 1)
            )
        );
        entityManager.clear();

        // when
        List<GameNewsLog> result =
            gameNewsLogRepository.findAllByGameSessionIdOrderByTurnNumberDescGameNewsLogIdDesc(1001L);

        // then
        assertThat(result).hasSize(2);
        assertThat(result)
            .extracting(GameNewsLog::getTurnNumber)
            .containsExactly(10, 9);
        assertThat(result)
            .extracting(GameNewsLog::getHeadlineSnapshot)
            .containsExactly("채용 한파 심화", "금리 인하 기조 지속");
    }
}
