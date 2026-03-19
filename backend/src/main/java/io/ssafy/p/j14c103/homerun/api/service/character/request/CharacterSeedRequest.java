package io.ssafy.p.j14c103.homerun.api.service.character.request;

import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;

public record CharacterSeedRequest(
    CharacterType characterType,
    JobType jobType,
    String seedType
) {

    public CharacterSeedRequest {
        if (characterType == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
        if (jobType == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
        if (seedType == null || seedType.isBlank()) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
    }

    public static CharacterSeedRequest of(
        final CharacterType characterType,
        final JobType jobType,
        final String seedType
    ) {
        return new CharacterSeedRequest(characterType, jobType, seedType);
    }
}
