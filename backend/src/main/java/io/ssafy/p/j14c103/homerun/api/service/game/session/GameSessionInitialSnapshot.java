package io.ssafy.p.j14c103.homerun.api.service.game.session;

import io.ssafy.p.j14c103.homerun.domain.character.CharacterSeedPolicy.CareerSeed;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterSeedPolicy.StatSeed;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CycleState;
import java.time.LocalDate;
import java.util.Objects;
import lombok.Getter;

@Getter
public class GameSessionInitialSnapshot {

    private final JobType jobType;
    private final Money cashBalance;
    private final Money totalAssets;
    private final Money netWorth;
    private final Money monthlyIncome;
    private final Money monthlyExpense;
    private final LocalDate currentDate;
    private final CycleState cycleState;
    private final StatSeed statSeed;
    private final CareerSeed careerSeed;

    private GameSessionInitialSnapshot(
        final JobType jobType,
        final Money cashBalance,
        final Money totalAssets,
        final Money netWorth,
        final Money monthlyIncome,
        final Money monthlyExpense,
        final LocalDate currentDate,
        final CycleState cycleState,
        final StatSeed statSeed,
        final CareerSeed careerSeed
    ) {
        this.jobType = Objects.requireNonNull(jobType);
        this.cashBalance = Objects.requireNonNull(cashBalance);
        this.totalAssets = Objects.requireNonNull(totalAssets);
        this.netWorth = Objects.requireNonNull(netWorth);
        this.monthlyIncome = Objects.requireNonNull(monthlyIncome);
        this.monthlyExpense = Objects.requireNonNull(monthlyExpense);
        this.currentDate = Objects.requireNonNull(currentDate);
        this.cycleState = Objects.requireNonNull(cycleState);
        this.statSeed = Objects.requireNonNull(statSeed);
        this.careerSeed = Objects.requireNonNull(careerSeed);
    }

    public static GameSessionInitialSnapshot of(
        final JobType jobType,
        final Money cashBalance,
        final Money totalAssets,
        final Money netWorth,
        final Money monthlyIncome,
        final Money monthlyExpense,
        final LocalDate currentDate,
        final CycleState cycleState,
        final StatSeed statSeed,
        final CareerSeed careerSeed
    ) {
        return new GameSessionInitialSnapshot(
            jobType,
            cashBalance,
            totalAssets,
            netWorth,
            monthlyIncome,
            monthlyExpense,
            currentDate,
            cycleState,
            statSeed,
            careerSeed
        );
    }
}
