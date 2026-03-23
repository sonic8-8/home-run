package io.ssafy.p.j14c103.homerun.api.service.game.start.response;

import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import lombok.Getter;

@Getter
public class ProfileOptionsResponse {

    private final List<ProfileOptionResponse> profiles;

    private ProfileOptionsResponse(final List<ProfileOptionResponse> profiles) {
        validateProfiles(profiles);
        this.profiles = List.copyOf(profiles);
    }

    public static ProfileOptionsResponse from(final List<ProfileOptionResponse> profiles) {
        return new ProfileOptionsResponse(profiles);
    }

    @Getter
    public static class ProfileOptionResponse {

        private final String profileCode;
        private final String name;
        private final JobType jobType;
        private final long annualSalary;
        private final long initialCash;
        private final ProfileStatsResponse stats;

        private ProfileOptionResponse(
            final String profileCode,
            final String name,
            final JobType jobType,
            final long annualSalary,
            final long initialCash,
            final ProfileStatsResponse stats
        ) {
            validateText(profileCode);
            validateText(name);
            validateJobType(jobType);
            validateNonNegative(annualSalary);
            validateNonNegative(initialCash);
            validateStats(stats);
            this.profileCode = profileCode;
            this.name = name;
            this.jobType = jobType;
            this.annualSalary = annualSalary;
            this.initialCash = initialCash;
            this.stats = stats;
        }

        public static ProfileOptionResponse of(
            final String profileCode,
            final String name,
            final JobType jobType,
            final long annualSalary,
            final long initialCash,
            final int salary,
            final int health,
            final int stability,
            final int growthSpeed,
            final int difficulty
        ) {
            return new ProfileOptionResponse(
                profileCode,
                name,
                jobType,
                annualSalary,
                initialCash,
                ProfileStatsResponse.of(salary, health, stability, growthSpeed, difficulty)
            );
        }
    }

    @Getter
    public static class ProfileStatsResponse {

        private final int salary;
        private final int health;
        private final int stability;
        private final int growthSpeed;
        private final int difficulty;

        private ProfileStatsResponse(
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

        public static ProfileStatsResponse of(
            final int salary,
            final int health,
            final int stability,
            final int growthSpeed,
            final int difficulty
        ) {
            return new ProfileStatsResponse(salary, health, stability, growthSpeed, difficulty);
        }
    }

    private static void validateProfiles(final List<ProfileOptionResponse> profiles) {
        if (profiles == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }

    private static void validateText(final String value) {
        if (value == null || value.isBlank()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }

    private static void validateJobType(final JobType jobType) {
        if (jobType == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }

    private static void validateStats(final ProfileStatsResponse stats) {
        if (stats == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }

    private static void validateNonNegative(final long value) {
        if (value < 0) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }

    private static void validateGauge(final int value) {
        if (value < 0 || value > 100) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }
}
