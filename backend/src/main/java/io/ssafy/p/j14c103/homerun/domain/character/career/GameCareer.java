package io.ssafy.p.j14c103.homerun.domain.character.career;

import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "게임커리어")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class GameCareer {

    @Id
    @Column(name = "게임번호", nullable = false)
    private Integer gameId;

    @Enumerated(EnumType.STRING)
    @Column(name = "직업유형")
    private JobType jobType;

    @Column(name = "직급")
    private String jobTitle;

    @Column(name = "연봉")
    private Integer salary;

    @Column(name = "근속턴수")
    private Integer tenureTurns;

    @Column(name = "최근12턴공부횟수")
    private Integer recentStudyCount;

    @Column(name = "최근12턴네트워킹횟수")
    private Integer recentNetworkingCount;

    @Column(name = "협상준비도점수")
    private Integer negotiationPreparationScore;

    @Column(name = "마지막협상턴")
    private Integer lastNegotiatedTurn;

    @Enumerated(EnumType.STRING)
    @Column(name = "고용상태")
    private EmploymentStatus employmentStatus;

    @Column(name = "수습종료턴")
    private Integer probationEndTurn;

    @Column(name = "재취업가능턴")
    private Integer rehireAvailableTurn;

    @Column(name = "실업급여잔여턴수")
    private Integer remainingUnemploymentBenefitTurns;

    @Column(name = "퇴사전연봉")
    private Integer salaryBeforeResignation;
}
