package io.ssafy.p.j14c103.homerun.api.service.character.career.response;

import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import java.util.List;

public record JobTypeOptionsResponse(
    List<JobTypeOptionResponse> jobTypes
) {

    public JobTypeOptionsResponse {
        if (jobTypes == null) {
            throw new IllegalArgumentException("jobTypes는 null일 수 없습니다.");
        }
        jobTypes = List.copyOf(jobTypes);
    }

    public static JobTypeOptionsResponse from(final List<JobTypeOptionResponse> jobTypes) {
        return new JobTypeOptionsResponse(jobTypes);
    }

    public record JobTypeOptionResponse(
        JobType jobType,
        String label,
        int salaryGauge,
        int healthGauge,
        int stabilityGauge,
        int growthSpeedGauge,
        int difficultyGauge
    ) {

        public JobTypeOptionResponse {
            if (jobType == null) {
                throw new IllegalArgumentException("jobType은 null일 수 없습니다.");
            }
            if (label == null || label.isBlank()) {
                throw new IllegalArgumentException("label은 비어 있을 수 없습니다.");
            }
            validateGauge("salaryGauge", salaryGauge);
            validateGauge("healthGauge", healthGauge);
            validateGauge("stabilityGauge", stabilityGauge);
            validateGauge("growthSpeedGauge", growthSpeedGauge);
            validateGauge("difficultyGauge", difficultyGauge);
        }

        public static JobTypeOptionResponse of(
            final JobType jobType,
            final String label,
            final int salaryGauge,
            final int healthGauge,
            final int stabilityGauge,
            final int growthSpeedGauge,
            final int difficultyGauge
        ) {
            return new JobTypeOptionResponse(
                jobType,
                label,
                salaryGauge,
                healthGauge,
                stabilityGauge,
                growthSpeedGauge,
                difficultyGauge
            );
        }

        private static void validateGauge(final String fieldName, final int gauge) {
            if (gauge < 0 || gauge > 100) {
                throw new IllegalArgumentException(fieldName + "는 0에서 100 사이여야 합니다.");
            }
        }
    }
}
