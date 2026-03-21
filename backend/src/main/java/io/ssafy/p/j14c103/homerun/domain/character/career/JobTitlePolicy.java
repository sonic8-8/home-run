package io.ssafy.p.j14c103.homerun.domain.character.career;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.Set;

public class JobTitlePolicy {

    private static final int INTERN_MAX_TENURE_TURNS = 6;
    private static final int STAFF_MAX_TENURE_TURNS = 23;
    private static final int ASSISTANT_MANAGER_MAX_TENURE_TURNS = 59;
    private static final int MANAGER_MAX_TENURE_TURNS = 107;
    private static final int DEPUTY_GENERAL_MANAGER_MAX_TENURE_TURNS = 167;

    private static final Set<JobType> CORPORATE_JOB_TYPES = Set.of(
        JobType.SMALL_BIZ,
        JobType.MID_BIZ,
        JobType.LARGE_BIZ,
        JobType.STARTUP
    );

    public String calculate(final JobType jobType, final int tenureTurns) {
        validateJobType(jobType);
        validateTenureTurns(tenureTurns);

        if (jobType == JobType.FREELANCER) {
            return calculateFreelancerJobTitle(tenureTurns);
        }
        if (!CORPORATE_JOB_TYPES.contains(jobType)) {
            throw new HomerunException(ErrorCode.CHARACTER_JOB_TYPE_UNSUPPORTED);
        }

        return calculateCorporateJobTitle(tenureTurns);
    }

    private void validateJobType(final JobType jobType) {
        if (jobType == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
    }

    private void validateTenureTurns(final int tenureTurns) {
        if (tenureTurns < 0) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
    }

    private String calculateCorporateJobTitle(final int tenureTurns) {
        if (tenureTurns <= INTERN_MAX_TENURE_TURNS) {
            return "수습/인턴";
        }
        if (tenureTurns <= STAFF_MAX_TENURE_TURNS) {
            return "사원";
        }
        if (tenureTurns <= ASSISTANT_MANAGER_MAX_TENURE_TURNS) {
            return "대리";
        }
        if (tenureTurns <= MANAGER_MAX_TENURE_TURNS) {
            return "과장";
        }
        if (tenureTurns <= DEPUTY_GENERAL_MANAGER_MAX_TENURE_TURNS) {
            return "차장";
        }

        return "부장";
    }

    private String calculateFreelancerJobTitle(final int tenureTurns) {
        if (tenureTurns <= INTERN_MAX_TENURE_TURNS) {
            return "신입";
        }
        if (tenureTurns <= STAFF_MAX_TENURE_TURNS) {
            return "주니어";
        }
        if (tenureTurns <= ASSISTANT_MANAGER_MAX_TENURE_TURNS) {
            return "미드레벨";
        }
        if (tenureTurns <= MANAGER_MAX_TENURE_TURNS) {
            return "시니어";
        }
        if (tenureTurns <= DEPUTY_GENERAL_MANAGER_MAX_TENURE_TURNS) {
            return "전문가";
        }

        return "마스터";
    }
}
