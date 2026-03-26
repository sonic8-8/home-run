package io.ssafy.p.j14c103.homerun.api.service.game.session.request;

import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateGameSessionServiceRequest {

    private Integer slotNumber;
    private CharacterType characterType;
    private String characterName;
    private JobType jobType;
    private String regionCode;
    private String districtCode;
    private Long targetPropertyId;
    private Boolean useMyData;

    @Builder(access = AccessLevel.PRIVATE)
    private CreateGameSessionServiceRequest(
        final Integer slotNumber,
        final CharacterType characterType,
        final String characterName,
        final JobType jobType,
        final String regionCode,
        final String districtCode,
        final Long targetPropertyId,
        final Boolean useMyData
    ) {
        this.slotNumber = slotNumber;
        this.characterType = characterType;
        this.characterName = characterName;
        this.jobType = jobType;
        this.regionCode = regionCode;
        this.districtCode = districtCode;
        this.targetPropertyId = targetPropertyId;
        this.useMyData = useMyData;
    }

    public static CreateGameSessionServiceRequest of(
        final Integer slotNumber,
        final CharacterType characterType,
        final String characterName,
        final JobType jobType,
        final String regionCode,
        final String districtCode,
        final Long targetPropertyId,
        final Boolean useMyData
    ) {
        return CreateGameSessionServiceRequest.builder()
            .slotNumber(slotNumber)
            .characterType(characterType)
            .characterName(characterName)
            .jobType(jobType)
            .regionCode(regionCode)
            .districtCode(districtCode)
            .targetPropertyId(targetPropertyId)
            .useMyData(useMyData)
            .build();
    }

    public DataSourceType toDataSourceType() {
        if (Boolean.TRUE.equals(useMyData)) {
            return DataSourceType.MY_DATA;
        }
        return DataSourceType.PROFILE;
    }
}
