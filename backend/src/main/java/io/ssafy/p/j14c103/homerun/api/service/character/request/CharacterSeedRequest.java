package io.ssafy.p.j14c103.homerun.api.service.character.request;

import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;

public record CharacterSeedRequest(
    CharacterType characterType,
    JobType jobType,
    String seedType
) {

    public CharacterSeedRequest {
        if (characterType == null) {
            throw new IllegalArgumentException("characterType은 null일 수 없습니다.");
        }
        if (jobType == null) {
            throw new IllegalArgumentException("jobType은 null일 수 없습니다.");
        }
        if (seedType == null || seedType.isBlank()) {
            throw new IllegalArgumentException("seedType은 비어 있을 수 없습니다.");
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
