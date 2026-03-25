package io.ssafy.p.j14c103.homerun.domain.history.news;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "game_news_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameNewsLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_news_log_id")
    private Integer gameNewsLogId;

    @Column(name = "game_session_id", nullable = false)
    private Long gameSessionId;

    @Column(name = "turn_number", nullable = false)
    private Integer turnNumber;

    @Column(name = "news_id", nullable = false)
    private String newsId;

    @Column(name = "headline_snapshot")
    private String headlineSnapshot;

    @Column(name = "published_date")
    private LocalDate publishedDate;

    private GameNewsLog(
        Long gameSessionId,
        Integer turnNumber,
        String newsId,
        String headlineSnapshot,
        LocalDate publishedDate
    ) {
        this.gameSessionId = gameSessionId;
        this.turnNumber = turnNumber;
        this.newsId = newsId;
        this.headlineSnapshot = headlineSnapshot;
        this.publishedDate = publishedDate;
    }

    public static GameNewsLog create(
        Long gameSessionId,
        Integer turnNumber,
        String newsId,
        String headlineSnapshot,
        LocalDate publishedDate
    ) {
        return new GameNewsLog(
            gameSessionId,
            turnNumber,
            newsId,
            headlineSnapshot,
            publishedDate
        );
    }
}
