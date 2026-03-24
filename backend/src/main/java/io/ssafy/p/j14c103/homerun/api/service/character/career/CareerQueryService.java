package io.ssafy.p.j14c103.homerun.api.service.character.career;

import io.ssafy.p.j14c103.homerun.api.service.character.career.response.JobTypeOptionsResponse;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterSeedPolicy;
import org.springframework.stereotype.Service;

@Service
public class CareerQueryService {

    private final CharacterSeedPolicy characterSeedPolicy = new CharacterSeedPolicy();

    public JobTypeOptionsResponse getJobTypeOptions() {
        return JobTypeOptionsResponse.from(
            characterSeedPolicy.getJobTypeProfiles().stream()
                .map(JobTypeOptionsResponse.JobTypeOptionResponse::from)
                .toList()
        );
    }
}
