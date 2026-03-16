package io.ssafy.p.j14c103.homerun.domain.character;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "게임스탯")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class GameStat {

    @Id
    @Column(name = "게임번호", nullable = false)
    private Integer gameId;

    @Column(name = "체력", nullable = false)
    private Integer health;

    @Column(name = "피로도", nullable = false)
    private Integer fatigue;

    @Column(name = "스트레스", nullable = false)
    private Integer stress;

    @Column(name = "행복도", nullable = false)
    private Integer happiness;

    @Column(name = "지식")
    private Integer knowledge;

    @Column(name = "번아웃여부")
    private Boolean burnout;

    @Column(name = "번아웃시작턴")
    private Integer burnoutStartedTurn;

    @Column(name = "입원종료턴")
    private Integer hospitalizedUntilTurn;
}
