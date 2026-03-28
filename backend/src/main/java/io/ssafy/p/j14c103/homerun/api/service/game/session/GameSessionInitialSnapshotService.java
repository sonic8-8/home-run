package io.ssafy.p.j14c103.homerun.api.service.game.session;

import io.ssafy.p.j14c103.homerun.api.service.financial.UserFinancialSummaryService;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterSeedPolicy;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.SeedType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialSummary;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetCardSpendRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetOtherIncomeRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetProfile;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetProfileRepository;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CycleState;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CycleType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.time.Clock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameSessionInitialSnapshotService {

    private static final CycleState INITIAL_CYCLE_STATE =
        CycleState.of(CyclePhase.RECOVERY, CycleType.CYCLE_RATE_HIKE, 18);

    private final UserFinancialSummaryService userFinancialSummaryService;
    private final UserAssetProfileRepository userAssetProfileRepository;
    private final UserAssetOtherIncomeRepository userAssetOtherIncomeRepository;
    private final UserAssetCardSpendRepository userAssetCardSpendRepository;
    private final Clock clock = Clock.systemDefaultZone();
    private final CharacterSeedPolicy characterSeedPolicy = new CharacterSeedPolicy();
    private final ProfileMonthlyExpensePolicy profileMonthlyExpensePolicy = new ProfileMonthlyExpensePolicy();

    public GameSessionInitialSnapshot read(
        final Long userId,
        final CharacterType characterType,
        final JobType jobType,
        final DataSourceType dataSourceType
    ) {
        if (dataSourceType == DataSourceType.MY_DATA) {
            return readMyData(userId);
        }
        return readProfile(characterType, jobType);
    }

    private GameSessionInitialSnapshot readMyData(final Long userId) {
        final UserAssetProfile profile = userAssetProfileRepository.findById(userId)
            .orElseThrow(() -> new HomerunException(ErrorCode.USER_ASSET_LINK_REQUIRED));
        final UserFinancialSummary summary = userFinancialSummaryService.getSummary(userId);

        return GameSessionInitialSnapshot.of(
            profile.getJobType(),
            Money.of(summary.getCashAssetAmount().longValue()),
            Money.of(summary.getTotalAssetAmount().longValue()),
            Money.of(summary.getNetAssetAmount().longValue()),
            Money.of(profile.getMonthlySalaryAmount() + getOtherIncomeAmount(userId)),
            Money.of(profile.getMonthlyFixedExpenseAmount() + getCardSpendAmount(userId)),
            LocalDate.now(clock),
            INITIAL_CYCLE_STATE
        );
    }

    private GameSessionInitialSnapshot readProfile(
        final CharacterType characterType,
        final JobType jobType
    ) {
        final CharacterSeedPolicy.CharacterSeedPlan seedPlan =
            characterSeedPolicy.calculate(characterType, jobType, SeedType.PROFILE);
        final Money initialCash = Money.of(seedPlan.session().initialCash());
        final Money initialNetWorth = Money.of(seedPlan.session().initialNetAssets());
        final Money monthlyIncome = Money.of(seedPlan.career().monthlySalary());

        return GameSessionInitialSnapshot.of(
            jobType,
            initialCash,
            initialNetWorth,
            initialNetWorth,
            monthlyIncome,
            profileMonthlyExpensePolicy.calculate(monthlyIncome),
            LocalDate.now(clock),
            INITIAL_CYCLE_STATE
        );
    }

    private int getOtherIncomeAmount(final Long userId) {
        return userAssetOtherIncomeRepository.findAllByUserIdOrderByIdAsc(userId).stream()
            .mapToInt(item -> item.getAmount().intValue())
            .sum();
    }

    private int getCardSpendAmount(final Long userId) {
        return userAssetCardSpendRepository.findAllByUserIdOrderByIdAsc(userId).stream()
            .mapToInt(item -> item.getAmount().intValue())
            .sum();
    }
}
