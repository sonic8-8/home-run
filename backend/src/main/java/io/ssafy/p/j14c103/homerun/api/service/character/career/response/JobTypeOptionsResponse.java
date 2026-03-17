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
        JobTypeStatsResponse stats
    ) {

        public JobTypeOptionResponse {
            if (jobType == null) {
                throw new IllegalArgumentException("jobType은 null일 수 없습니다.");
            }
            if (label == null || label.isBlank()) {
                throw new IllegalArgumentException("label은 비어 있을 수 없습니다.");
            }
            if (stats == null) {
                throw new IllegalArgumentException("stats는 null일 수 없습니다.");
            }
        }

        public static JobTypeOptionResponse of(
            final JobType jobType,
            final String label,
            final int salary,
            final int health,
            final int stability,
            final int growthSpeed,
            final int difficulty
        ) {
            return new JobTypeOptionResponse(
                jobType,
                label,
                JobTypeStatsResponse.of(salary, health, stability, growthSpeed, difficulty)
            );
        }
    }

    public record JobTypeStatsResponse(
        int salary,
        int health,
        int stability,
        int growthSpeed,
        int difficulty
    ) {

        public JobTypeStatsResponse {
            validateGauge("salary", salary);
            validateGauge("health", health);
            validateGauge("stability", stability);
            validateGauge("growthSpeed", growthSpeed);
            validateGauge("difficulty", difficulty);
        }

        public static JobTypeStatsResponse of(
            final int salary,
            final int health,
            final int stability,
            final int growthSpeed,
            final int difficulty
        ) {
            return new JobTypeStatsResponse(salary, health, stability, growthSpeed, difficulty);
        }
    }

    private static void validateGauge(final String fieldName, final int gauge) {
        if (gauge < 0 || gauge > 100) {
            throw new IllegalArgumentException(fieldName + "는 0에서 100 사이여야 합니다.");
        }
    }
}
