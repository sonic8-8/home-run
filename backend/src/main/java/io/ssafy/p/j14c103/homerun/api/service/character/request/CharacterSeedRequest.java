package io.ssafy.p.j14c103.homerun.api.service.character.request;

import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CharacterSeedRequest {

    private CharacterType characterType;
    private JobType jobType;
    private String seedType;

    @Builder(access = AccessLevel.PRIVATE)
    private CharacterSeedRequest(
        final CharacterType characterType,
        final JobType jobType,
        final String seedType
    ) {
        validateRequest(characterType, jobType, seedType);

        this.characterType = characterType;
        this.jobType = jobType;
        this.seedType = seedType;
    }

    public static CharacterSeedRequest of(
        final CharacterType characterType,
        final JobType jobType,
        final String seedType
    ) {
        return CharacterSeedRequest.builder()
            .characterType(characterType)
            .jobType(jobType)
            .seedType(seedType)
            .build();
    }

    private void validateRequest(
        final CharacterType characterType,
        final JobType jobType,
        final String seedType
    ) {
        if (characterType == null || jobType == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
        if (seedType == null || seedType.isBlank()) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
    }
}
