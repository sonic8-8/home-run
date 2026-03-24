package io.ssafy.p.j14c103.homerun.api.service.character.career.response;

import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class JobTypeOptionsResponse {

    private final List<JobTypeOptionResponse> jobTypes;

    @Builder(access = AccessLevel.PRIVATE)
    private JobTypeOptionsResponse(final List<JobTypeOptionResponse> jobTypes) {
        validateRequest(jobTypes);

        this.jobTypes = List.copyOf(jobTypes);
    }

    public static JobTypeOptionsResponse from(final List<JobTypeOptionResponse> jobTypes) {
        return JobTypeOptionsResponse.builder()
            .jobTypes(jobTypes)
            .build();
    }

    private void validateRequest(final List<JobTypeOptionResponse> jobTypes) {
        if (jobTypes == null) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
    }

    @Getter
    public static class JobTypeOptionResponse {

        private final JobType jobType;
        private final String label;
        private final JobTypeStatsResponse stats;

        @Builder(access = AccessLevel.PRIVATE)
        private JobTypeOptionResponse(
            final JobType jobType,
            final String label,
            final JobTypeStatsResponse stats
        ) {
            validateRequest(jobType, label, stats);

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
            return JobTypeOptionResponse.builder()
                .jobType(jobType)
                .label(label)
                .stats(JobTypeStatsResponse.of(
                    salary,
                    health,
                    stability,
                    growthSpeed,
                    difficulty
                ))
                .build();
        }

        public static JobTypeOptionResponse from(
            final io.ssafy.p.j14c103.homerun.domain.character.CharacterSeedPolicy.JobTypeSeedProfile
                profile
        ) {
            if (profile == null) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }

            return JobTypeOptionResponse.builder()
                .jobType(profile.jobType())
                .label(profile.label())
                .stats(JobTypeStatsResponse.from(profile.gauge()))
                .build();
        }

        private void validateRequest(
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
        }
    }

    @Getter
    public static class JobTypeStatsResponse {

        private final int salary;
        private final int health;
        private final int stability;
        private final int growthSpeed;
        private final int difficulty;

        @Builder(access = AccessLevel.PRIVATE)
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
            return JobTypeStatsResponse.builder()
                .salary(salary)
                .health(health)
                .stability(stability)
                .growthSpeed(growthSpeed)
                .difficulty(difficulty)
                .build();
        }

        private static JobTypeStatsResponse from(
            final io.ssafy.p.j14c103.homerun.domain.character.CharacterSeedPolicy.JobTypeGauge gauge
        ) {
            if (gauge == null) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }

            return JobTypeStatsResponse.builder()
                .salary(gauge.salary())
                .health(gauge.health())
                .stability(gauge.stability())
                .growthSpeed(gauge.growthSpeed())
                .difficulty(gauge.difficulty())
                .build();
        }
    }

    private static void validateGauge(final int gauge) {
        if (gauge < 0 || gauge > 100) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
    }
}
