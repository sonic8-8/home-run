package io.ssafy.p.j14c103.homerun.api.service.character.career.response;

import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import lombok.Getter;

@Getter
public class JobTypeOptionsResponse {

    private final List<JobTypeOptionResponse> jobTypes;

    private JobTypeOptionsResponse(final List<JobTypeOptionResponse> jobTypes) {
        if (jobTypes == null) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
        this.jobTypes = List.copyOf(jobTypes);
    }

    public static JobTypeOptionsResponse from(final List<JobTypeOptionResponse> jobTypes) {
        return new JobTypeOptionsResponse(jobTypes);
    }

    public List<JobTypeOptionResponse> jobTypes() {
        return jobTypes;
    }

    @Getter
    public static class JobTypeOptionResponse {

        private final JobType jobType;
        private final String label;
        private final JobTypeStatsResponse stats;

        private JobTypeOptionResponse(
            final JobType jobType,
            final String label,
            final JobTypeStatsResponse stats
        ) {
            if (jobType == null) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }
            if (label == null || label.isBlank()) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }
            if (stats == null) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }

            this.jobType = jobType;
            this.label = label;
            this.stats = stats;
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

        public JobType jobType() {
            return jobType;
        }

        public String label() {
            return label;
        }

        public JobTypeStatsResponse stats() {
            return stats;
        }
    }

    @Getter
    public static class JobTypeStatsResponse {

        private final int salary;
        private final int health;
        private final int stability;
        private final int growthSpeed;
        private final int difficulty;

        private JobTypeStatsResponse(
            final int salary,
            final int health,
            final int stability,
            final int growthSpeed,
            final int difficulty
        ) {
            validateGauge(salary);
            validateGauge(health);
            validateGauge(stability);
            validateGauge(growthSpeed);
            validateGauge(difficulty);

            this.salary = salary;
            this.health = health;
            this.stability = stability;
            this.growthSpeed = growthSpeed;
            this.difficulty = difficulty;
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

        public int salary() {
            return salary;
        }

        public int health() {
            return health;
        }

        public int stability() {
            return stability;
        }

        public int growthSpeed() {
            return growthSpeed;
        }

        public int difficulty() {
            return difficulty;
        }
    }

    private static void validateGauge(final int gauge) {
        if (gauge < 0 || gauge > 100) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
    }
}
