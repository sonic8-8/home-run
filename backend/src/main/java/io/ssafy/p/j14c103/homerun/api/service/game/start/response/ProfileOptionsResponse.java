package io.ssafy.p.j14c103.homerun.api.service.game.start.response;

import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;

public record ProfileOptionsResponse(
    List<ProfileOptionResponse> profiles
) {

    public ProfileOptionsResponse {
        if (profiles == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
        profiles = List.copyOf(profiles);
    }

    public static ProfileOptionsResponse from(final List<ProfileOptionResponse> profiles) {
        return new ProfileOptionsResponse(profiles);
    }

    public record ProfileOptionResponse(
        String profileCode,
        String name,
        JobType jobType,
        long annualSalary,
        long initialCash,
        ProfileStatsResponse stats
    ) {

        public ProfileOptionResponse {
            validateText(profileCode);
            validateText(name);
            if (jobType == null) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
            validateNonNegative(annualSalary);
            validateNonNegative(initialCash);
            if (stats == null) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
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

    public record ProfileStatsResponse(
        int salary,
        int health,
        int stability,
        int growthSpeed,
        int difficulty
    ) {

        public ProfileStatsResponse {
            validateGauge(salary);
            validateGauge(health);
            validateGauge(stability);
            validateGauge(growthSpeed);
            validateGauge(difficulty);
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

    private static void validateText(final String value) {
        if (value == null || value.isBlank()) {
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
