package io.ssafy.p.j14c103.homerun.domain.character.career;

import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
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

    private static final int INITIAL_TENURE_TURNS = 0;

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

    public void acceptTransfer(
        final JobTransferPolicy.JobOffer offer,
        final JobTitlePolicy jobTitlePolicy,
        final int currentTurn
    ) {
        validateOffer(offer);
        validateJobTitlePolicy(jobTitlePolicy);
        validateCurrentTurn(currentTurn);

        this.jobType = offer.jobType();
        this.jobTitle = jobTitlePolicy.calculate(offer.jobType(), INITIAL_TENURE_TURNS);
        this.salary = offer.offeredSalary();
        this.tenureTurns = INITIAL_TENURE_TURNS;
        this.rehireAvailableTurn = null;
        this.remainingUnemploymentBenefitTurns = 0;
        this.salaryBeforeResignation = null;

        if (offer.probationTurns() == null) {
            this.employmentStatus = EmploymentStatus.EMPLOYED;
            this.probationEndTurn = null;
            return;
        }

        startProbation(currentTurn + offer.probationTurns());
    }

    public void startProbation(final int probationEndTurn) {
        validateCurrentTurn(probationEndTurn);
        this.employmentStatus = EmploymentStatus.PROBATION;
        this.probationEndTurn = probationEndTurn;
    }

    private void validateOffer(final JobTransferPolicy.JobOffer offer) {
        if (offer == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
    }

    private void validateJobTitlePolicy(final JobTitlePolicy jobTitlePolicy) {
        if (jobTitlePolicy == null) {
            throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
        }
    }

    private void validateCurrentTurn(final int currentTurn) {
        if (currentTurn < 1) {
            throw new HomerunException(ErrorCode.CHARACTER_TURN_INVALID);
        }
    }
}
