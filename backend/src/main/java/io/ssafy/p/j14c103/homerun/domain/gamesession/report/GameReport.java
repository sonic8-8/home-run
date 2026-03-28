package io.ssafy.p.j14c103.homerun.domain.gamesession.report;

import io.ssafy.p.j14c103.homerun.domain.gamesession.SessionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "game_reports")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameReport {

    @Id
    @Column(name = "game_session_id")
    private Long gameSessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ending_type")
    private SessionStatus endingType;

    @Column(name = "ending_title")
    private String endingTitle;

    @Column(name = "total_income_amount")
    private Integer totalIncomeAmount;

    @Column(name = "total_expense_amount")
    private Integer totalExpenseAmount;

    @Column(name = "grade")
    private String grade;

    @Column(name = "total_assets_amount")
    private Integer totalAssetsAmount;

    @Column(name = "net_profit_amount")
    private Integer netProfitAmount;

    @Column(name = "top_spending_category")
    private String topSpendingCategory;

    @Column(name = "top_spending_ratio")
    private BigDecimal topSpendingRatio;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "achievements")
    private List<GameReportAchievement> achievements;

    private GameReport(
        final Long gameSessionId,
        final SessionStatus endingType,
        final String endingTitle,
        final Integer totalIncomeAmount,
        final Integer totalExpenseAmount,
        final String grade,
        final Integer totalAssetsAmount,
        final Integer netProfitAmount,
        final String topSpendingCategory,
        final BigDecimal topSpendingRatio,
        final List<GameReportAchievement> achievements
    ) {
        this.gameSessionId = gameSessionId;
        this.endingType = endingType;
        this.endingTitle = endingTitle;
        this.totalIncomeAmount = totalIncomeAmount;
        this.totalExpenseAmount = totalExpenseAmount;
        this.grade = grade;
        this.totalAssetsAmount = totalAssetsAmount;
        this.netProfitAmount = netProfitAmount;
        this.topSpendingCategory = topSpendingCategory;
        this.topSpendingRatio = topSpendingRatio;
        this.achievements = achievements;
    }

    public static GameReport create(
        final Long gameSessionId,
        final SessionStatus endingType,
        final String endingTitle,
        final Integer totalIncomeAmount,
        final Integer totalExpenseAmount,
        final String grade,
        final Integer totalAssetsAmount,
        final Integer netProfitAmount,
        final String topSpendingCategory,
        final BigDecimal topSpendingRatio,
        final List<GameReportAchievement> achievements
    ) {
        return new GameReport(
            gameSessionId,
            endingType,
            endingTitle,
            totalIncomeAmount,
            totalExpenseAmount,
            grade,
            totalAssetsAmount,
            netProfitAmount,
            topSpendingCategory,
            topSpendingRatio,
            List.copyOf(achievements)
        );
    }
}
