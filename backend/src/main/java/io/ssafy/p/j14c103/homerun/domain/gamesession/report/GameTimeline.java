package io.ssafy.p.j14c103.homerun.domain.gamesession.report;

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
@Table(name = "game_timelines")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameTimeline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_timeline_id")
    private Long gameTimelineId;

    @Column(name = "game_session_id", nullable = false)
    private Long gameSessionId;

    @Column(name = "turn_number", nullable = false)
    private Integer turnNumber;

    @Column(name = "logged_date")
    private LocalDate loggedDate;

    @Column(name = "cash")
    private Integer cash;

    @Column(name = "net_assets")
    private Integer netAssets;

    @Column(name = "total_assets")
    private Integer totalAssets;

    @Column(name = "stock_value_amount")
    private Integer stockValueAmount;

    @Column(name = "loan_balance_amount")
    private Integer loanBalanceAmount;

    @Column(name = "salary_amount")
    private Integer salaryAmount;

    private GameTimeline(
        final Long gameSessionId,
        final Integer turnNumber,
        final LocalDate loggedDate,
        final Integer cash,
        final Integer netAssets,
        final Integer totalAssets,
        final Integer stockValueAmount,
        final Integer loanBalanceAmount,
        final Integer salaryAmount
    ) {
        this.gameSessionId = gameSessionId;
        this.turnNumber = turnNumber;
        this.loggedDate = loggedDate;
        this.cash = cash;
        this.netAssets = netAssets;
        this.totalAssets = totalAssets;
        this.stockValueAmount = stockValueAmount;
        this.loanBalanceAmount = loanBalanceAmount;
        this.salaryAmount = salaryAmount;
    }

    public static GameTimeline create(
        final Long gameSessionId,
        final Integer turnNumber,
        final LocalDate loggedDate,
        final Integer cash,
        final Integer netAssets,
        final Integer totalAssets,
        final Integer stockValueAmount,
        final Integer loanBalanceAmount,
        final Integer salaryAmount
    ) {
        return new GameTimeline(
            gameSessionId,
            turnNumber,
            loggedDate,
            cash,
            netAssets,
            totalAssets,
            stockValueAmount,
            loanBalanceAmount,
            salaryAmount
        );
    }
}
