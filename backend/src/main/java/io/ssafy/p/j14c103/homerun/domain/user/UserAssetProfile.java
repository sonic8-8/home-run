package io.ssafy.p.j14c103.homerun.domain.user;

import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "user_asset_profiles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAssetProfile {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "main_account_balance_amount", nullable = false)
    private Integer mainAccountBalanceAmount;

    @Column(name = "salary_day_of_month", nullable = false)
    private Integer salaryDayOfMonth;

    @Column(name = "monthly_salary_amount", nullable = false)
    private Integer monthlySalaryAmount;

    @Column(name = "monthly_fixed_expense_amount", nullable = false)
    private Integer monthlyFixedExpenseAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 30)
    private JobType jobType;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private UserAssetProfile(
            final Long userId,
            final Integer mainAccountBalanceAmount,
            final Integer salaryDayOfMonth,
            final Integer monthlySalaryAmount,
            final Integer monthlyFixedExpenseAmount,
            final JobType jobType
    ) {
        validate(
                userId,
                mainAccountBalanceAmount,
                salaryDayOfMonth,
                monthlySalaryAmount,
                monthlyFixedExpenseAmount,
                jobType
        );
        this.userId = userId;
        this.mainAccountBalanceAmount = mainAccountBalanceAmount;
        this.salaryDayOfMonth = salaryDayOfMonth;
        this.monthlySalaryAmount = monthlySalaryAmount;
        this.monthlyFixedExpenseAmount = monthlyFixedExpenseAmount;
        this.jobType = jobType;
        this.updatedAt = LocalDateTime.now();
    }

    public static UserAssetProfile create(
            final Long userId,
            final Integer mainAccountBalanceAmount,
            final Integer salaryDayOfMonth,
            final Integer monthlySalaryAmount,
            final Integer monthlyFixedExpenseAmount,
            final JobType jobType
    ) {
        return new UserAssetProfile(
                userId,
                mainAccountBalanceAmount,
                salaryDayOfMonth,
                monthlySalaryAmount,
                monthlyFixedExpenseAmount,
                jobType
        );
    }

    public void update(
            final Integer mainAccountBalanceAmount,
            final Integer salaryDayOfMonth,
            final Integer monthlySalaryAmount,
            final Integer monthlyFixedExpenseAmount,
            final JobType jobType
    ) {
        validate(
                userId,
                mainAccountBalanceAmount,
                salaryDayOfMonth,
                monthlySalaryAmount,
                monthlyFixedExpenseAmount,
                jobType
        );
        this.mainAccountBalanceAmount = mainAccountBalanceAmount;
        this.salaryDayOfMonth = salaryDayOfMonth;
        this.monthlySalaryAmount = monthlySalaryAmount;
        this.monthlyFixedExpenseAmount = monthlyFixedExpenseAmount;
        this.jobType = jobType;
        this.updatedAt = LocalDateTime.now();
    }

    private void validate(
            final Long userId,
            final Integer mainAccountBalanceAmount,
            final Integer salaryDayOfMonth,
            final Integer monthlySalaryAmount,
            final Integer monthlyFixedExpenseAmount,
            final JobType jobType
    ) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (mainAccountBalanceAmount == null || mainAccountBalanceAmount < 0) {
            throw new IllegalArgumentException("수시입출금 계좌 잔액은 0 이상이어야 합니다.");
        }
        if (salaryDayOfMonth == null || salaryDayOfMonth < 1 || salaryDayOfMonth > 28) {
            throw new IllegalArgumentException("급여일은 1일부터 28일 사이여야 합니다.");
        }
        if (monthlySalaryAmount == null || monthlySalaryAmount < 0) {
            throw new IllegalArgumentException("월 급여액은 0 이상이어야 합니다.");
        }
        if (monthlyFixedExpenseAmount == null || monthlyFixedExpenseAmount < 0) {
            throw new IllegalArgumentException("고정 지출은 0 이상이어야 합니다.");
        }
        if (jobType == null) {
            throw new IllegalArgumentException("직장 유형은 필수입니다.");
        }
    }
}
