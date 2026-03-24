package io.ssafy.p.j14c103.homerun.domain.history;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "게임플레이이력")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class GameplayHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "게임플레이이력번호", nullable = false)
    private Integer historyId;

    @Column(name = "게임번호", nullable = false)
    private Integer gameId;

    @Column(name = "이벤트번호", nullable = false)
    private Integer eventId;

    @Column(name = "테이블명")
    private String tableName;

    @Column(name = "컬럼명")
    private String columnName;

    @Column(name = "변경테이블주요키1")
    private String targetKey1;

    @Column(name = "변경테이블주요키2")
    private String targetKey2;

    @Column(name = "변경테이블주요키3")
    private String targetKey3;

    @Lob
    @Column(name = "변경전데이터")
    private String beforeValue;

    @Lob
    @Column(name = "변경후데이터")
    private String afterValue;

    @Column(name = "시작일시")
    private LocalDateTime startedAt;

    @Column(name = "종료일시")
    private LocalDateTime endedAt;

    @Column(name = "선택지번호")
    private Integer choiceId;

    @Lob
    @Column(name = "결과효과정보")
    private String effectPayload;

    @Lob
    @Column(name = "결과요약")
    private String summary;

    @Column(name = "이력발생턴")
    private Integer occurredTurn;
}
