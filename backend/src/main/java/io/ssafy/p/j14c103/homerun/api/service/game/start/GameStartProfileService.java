package io.ssafy.p.j14c103.homerun.api.service.game.start;

import static java.util.function.Function.identity;

import io.ssafy.p.j14c103.homerun.api.service.character.career.CareerQueryService;
import io.ssafy.p.j14c103.homerun.api.service.character.career.response.JobTypeOptionsResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.start.response.ProfileOptionsResponse;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterSeedPolicy;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.SeedType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GameStartProfileService {

    private static final CharacterType PROFILE_REFERENCE_CHARACTER_TYPE = CharacterType.FEMALE;
    private static final List<ProfileDefinition> PROFILE_DEFINITIONS = List.of(
        ProfileDefinition.of("JUNIOR_DEVELOPER", "신입 개발자", JobType.MID_BIZ),
        ProfileDefinition.of("CORPORATE_OFFICE_WORKER", "대기업 사무직", JobType.LARGE_BIZ),
        ProfileDefinition.of("IT_STARTUP", "IT 스타트업", JobType.STARTUP)
    );

    private final CareerQueryService careerQueryService;
    private final CharacterSeedPolicy characterSeedPolicy = new CharacterSeedPolicy();

    public ProfileOptionsResponse getProfileOptions() {
        final Map<JobType, JobTypeOptionsResponse.JobTypeOptionResponse> jobTypeOptions =
            careerQueryService.getJobTypeOptions().jobTypes().stream()
                .collect(java.util.stream.Collectors.toMap(
                    JobTypeOptionsResponse.JobTypeOptionResponse::jobType,
                    identity()
                ));
        final Map<JobType, CharacterSeedPolicy.JobTypeSeedProfile> seedProfiles =
            characterSeedPolicy.getJobTypeProfiles().stream()
                .collect(java.util.stream.Collectors.toMap(
                    CharacterSeedPolicy.JobTypeSeedProfile::jobType,
                    identity()
                ));

        final List<ProfileOptionsResponse.ProfileOptionResponse> profiles = PROFILE_DEFINITIONS.stream()
            .map(definition -> toProfileOption(definition, jobTypeOptions, seedProfiles))
            .toList();

        return ProfileOptionsResponse.from(profiles);
    }

    private ProfileOptionsResponse.ProfileOptionResponse toProfileOption(
        final ProfileDefinition definition,
        final Map<JobType, JobTypeOptionsResponse.JobTypeOptionResponse> jobTypeOptions,
        final Map<JobType, CharacterSeedPolicy.JobTypeSeedProfile> seedProfiles
    ) {
        final JobTypeOptionsResponse.JobTypeOptionResponse jobTypeOption =
            findJobTypeOption(definition.getJobType(), jobTypeOptions);
        final CharacterSeedPolicy.JobTypeSeedProfile seedProfile =
            findSeedProfile(definition.getJobType(), seedProfiles);
        final CharacterSeedPolicy.CharacterSeedPlan profileSeed = characterSeedPolicy.calculate(
            PROFILE_REFERENCE_CHARACTER_TYPE,
            definition.getJobType(),
            SeedType.PROFILE
        );

        return ProfileOptionsResponse.ProfileOptionResponse.of(
            definition.getProfileCode(),
            definition.getName(),
            definition.getJobType(),
            seedProfile.initialAnnualSalary(),
            profileSeed.session().initialCash(),
            jobTypeOption.stats().salary(),
            jobTypeOption.stats().health(),
            jobTypeOption.stats().stability(),
            jobTypeOption.stats().growthSpeed(),
            jobTypeOption.stats().difficulty()
        );
    }

    private JobTypeOptionsResponse.JobTypeOptionResponse findJobTypeOption(
        final JobType jobType,
        final Map<JobType, JobTypeOptionsResponse.JobTypeOptionResponse> jobTypeOptions
    ) {
        final JobTypeOptionsResponse.JobTypeOptionResponse option = jobTypeOptions.get(jobType);
        if (option != null) {
            return option;
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    private CharacterSeedPolicy.JobTypeSeedProfile findSeedProfile(
        final JobType jobType,
        final Map<JobType, CharacterSeedPolicy.JobTypeSeedProfile> seedProfiles
    ) {
        final CharacterSeedPolicy.JobTypeSeedProfile profile = seedProfiles.get(jobType);
        if (profile != null) {
            return profile;
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    @Getter
    private static class ProfileDefinition {

        private final String profileCode;
        private final String name;
        private final JobType jobType;

        private ProfileDefinition(
            final String profileCode,
            final String name,
            final JobType jobType
        ) {
            this.profileCode = profileCode;
            this.name = name;
            this.jobType = jobType;
        }

        public static ProfileDefinition of(
            final String profileCode,
            final String name,
            final JobType jobType
        ) {
            return new ProfileDefinition(profileCode, name, jobType);
        }
    }
}
