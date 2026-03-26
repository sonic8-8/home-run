package io.ssafy.p.j14c103.homerun.domain.world.cycle;

import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CycleJobImpactPolicy {

    private final Map<CycleType, Map<JobType, CycleJobImpact>> impactTable;

    public CycleJobImpactPolicy() {
        this.impactTable = createImpactTable();
    }

    public CycleJobImpact resolve(
        final CycleType cycleType,
        final JobType jobType
    ) {
        final CycleType currentCycleType = requireCycleType(cycleType);
        final JobType currentJobType = normalizeJobType(jobType);
        final Map<JobType, CycleJobImpact> impactsByJobType = impactTable.get(currentCycleType);

        if (impactsByJobType != null && impactsByJobType.containsKey(currentJobType)) {
            return impactsByJobType.get(currentJobType);
        }
        throw new HomerunException(ErrorCode.CHARACTER_JOB_TYPE_UNSUPPORTED);
    }

    private Map<CycleType, Map<JobType, CycleJobImpact>> createImpactTable() {
        final Map<CycleType, Map<JobType, CycleJobImpact>> table = new EnumMap<>(CycleType.class);

        table.put(CycleType.CYCLE_BOOM, impacts(
            impact(JobType.SMALL_BIZ, "1.3", "0.7", 0),
            impact(JobType.MID_BIZ, "1.4", "0.6", 0),
            impact(JobType.LARGE_BIZ, "1.2", "0.5", 0),
            impact(JobType.FREELANCER, "1.5", null, -1)
        ));
        table.put(CycleType.CYCLE_FINANCIAL_CRISIS, impacts(
            impact(JobType.SMALL_BIZ, "0.5", "2.5", 2),
            impact(JobType.MID_BIZ, "0.6", "2.0", 2),
            impact(JobType.LARGE_BIZ, "0.7", "1.5", 1),
            impact(JobType.FREELANCER, "0.4", null, 3)
        ));
        table.put(CycleType.CYCLE_CURRENCY_CRISIS, impacts(
            impact(JobType.SMALL_BIZ, "0.4", "3.0", 3),
            impact(JobType.MID_BIZ, "0.5", "2.5", 2),
            impact(JobType.LARGE_BIZ, "0.6", "2.0", 2),
            impact(JobType.FREELANCER, "0.3", null, 3)
        ));
        table.put(CycleType.CYCLE_GREAT_DEPRESSION, impacts(
            impact(JobType.SMALL_BIZ, "0.3", "3.5", 4),
            impact(JobType.MID_BIZ, "0.4", "3.0", 3),
            impact(JobType.LARGE_BIZ, "0.5", "2.0", 2),
            impact(JobType.FREELANCER, "0.2", null, 4)
        ));
        table.put(CycleType.CYCLE_TECH_BUBBLE, impacts(
            impact(JobType.SMALL_BIZ, "0.7", "1.8", 1),
            impact(JobType.MID_BIZ, "0.8", "1.5", 1),
            impact(JobType.LARGE_BIZ, "0.8", "1.3", 1),
            impact(JobType.FREELANCER, "0.5", null, 2)
        ));
        table.put(CycleType.CYCLE_OIL_SHOCK, impacts(
            impact(JobType.SMALL_BIZ, "0.6", "2.0", 2),
            impact(JobType.MID_BIZ, "0.7", "1.8", 1),
            impact(JobType.LARGE_BIZ, "0.8", "1.3", 1),
            impact(JobType.FREELANCER, "0.6", null, 2)
        ));
        table.put(CycleType.CYCLE_STAGFLATION, impacts(
            impact(JobType.SMALL_BIZ, "0.5", "2.2", 2),
            impact(JobType.MID_BIZ, "0.6", "1.8", 2),
            impact(JobType.LARGE_BIZ, "0.7", "1.5", 1),
            impact(JobType.FREELANCER, "0.5", null, 2)
        ));
        table.put(CycleType.CYCLE_PANDEMIC, impacts(
            impact(JobType.SMALL_BIZ, "0.5", "2.5", 2),
            impact(JobType.MID_BIZ, "0.6", "2.0", 1),
            impact(JobType.LARGE_BIZ, "0.8", "1.3", 1),
            impact(JobType.FREELANCER, "0.4", null, 2)
        ));
        table.put(CycleType.CYCLE_GEOPOLITICAL, impacts(
            impact(JobType.SMALL_BIZ, "0.7", "1.8", 1),
            impact(JobType.MID_BIZ, "0.75", "1.5", 1),
            impact(JobType.LARGE_BIZ, "0.85", "1.2", 1),
            impact(JobType.FREELANCER, "0.6", null, 2)
        ));
        table.put(CycleType.CYCLE_RATE_HIKE, impacts(
            impact(JobType.SMALL_BIZ, "0.7", "1.5", 1),
            impact(JobType.MID_BIZ, "0.8", "1.3", 1),
            impact(JobType.LARGE_BIZ, "0.85", "1.1", 0),
            impact(JobType.FREELANCER, "0.6", null, 1)
        ));

        return table;
    }

    private Map<JobType, CycleJobImpact> impacts(
        final JobImpactEntry... entries
    ) {
        final Map<JobType, CycleJobImpact> impacts = new EnumMap<>(JobType.class);
        for (JobImpactEntry entry : entries) {
            impacts.put(entry.jobType(), entry.impact());
        }
        return impacts;
    }

    private JobImpactEntry impact(
        final JobType jobType,
        final String salaryMultiplier,
        final String layoffMultiplier,
        final int rehirePenaltyTurns
    ) {
        return new JobImpactEntry(
            jobType,
            CycleJobImpact.of(
                new BigDecimal(salaryMultiplier),
                toBigDecimal(layoffMultiplier),
                rehirePenaltyTurns
            )
        );
    }

    private BigDecimal toBigDecimal(final String value) {
        if (value == null) {
            return null;
        }
        return new BigDecimal(value);
    }

    private CycleType requireCycleType(final CycleType cycleType) {
        if (cycleType != null) {
            return cycleType;
        }
        throw new HomerunException(ErrorCode.WORLD_CYCLE_INPUT_INVALID);
    }

    private JobType normalizeJobType(final JobType jobType) {
        if (jobType == JobType.STARTUP) {
            return JobType.MID_BIZ;
        }
        if (jobType != null) {
            return jobType;
        }
        throw new HomerunException(ErrorCode.CHARACTER_JOB_TYPE_UNSUPPORTED);
    }

    private static class JobImpactEntry {

        private final JobType jobType;
        private final CycleJobImpact impact;

        private JobImpactEntry(
            final JobType jobType,
            final CycleJobImpact impact
        ) {
            this.jobType = jobType;
            this.impact = impact;
        }

        public JobType jobType() {
            return jobType;
        }

        public CycleJobImpact impact() {
            return impact;
        }
    }
}
