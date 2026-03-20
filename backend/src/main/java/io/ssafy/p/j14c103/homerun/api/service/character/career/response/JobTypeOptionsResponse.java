package io.ssafy.p.j14c103.homerun.api.service.character.career.response;

import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;

public record JobTypeOptionsResponse(
    List<JobTypeOptionResponse> jobTypes
) {

    public JobTypeOptionsResponse {
        if (jobTypes == null) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
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
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }
            if (label == null || label.isBlank()) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }
            if (stats == null) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
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
            validateGauge(salary);
            validateGauge(health);
            validateGauge(stability);
            validateGauge(growthSpeed);
            validateGauge(difficulty);
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

    private static void validateGauge(final int gauge) {
        if (gauge < 0 || gauge > 100) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
    }
}
