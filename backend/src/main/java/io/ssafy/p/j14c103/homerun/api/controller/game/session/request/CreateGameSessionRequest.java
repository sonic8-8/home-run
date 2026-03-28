package io.ssafy.p.j14c103.homerun.api.controller.game.session.request;

import io.ssafy.p.j14c103.homerun.api.service.game.session.request.CreateGameSessionServiceRequest;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@ValidCreateGameSessionRequest
public class CreateGameSessionRequest {

    @NotNull(message = "{validation.game.session.create.slotNumber.notNull}")
    @Min(value = 1, message = "{validation.game.session.create.slotNumber.min}")
    @Max(value = 3, message = "{validation.game.session.create.slotNumber.max}")
    private Integer slotNumber;

    @NotNull(message = "{validation.game.session.create.characterType.notNull}")
    private CharacterType characterType;

    @NotBlank(message = "{validation.game.session.create.characterName.notBlank}")
    @Size(max = 100, message = "{validation.game.session.create.characterName.size}")
    private String characterName;

    private JobType jobType;

    @NotBlank(message = "{validation.game.session.create.regionCode.notBlank}")
    private String regionCode;

    @NotBlank(message = "{validation.game.session.create.districtCode.notBlank}")
    private String districtCode;

    @NotNull(message = "{validation.game.session.create.targetPropertyId.notNull}")
    @Positive(message = "{validation.game.session.create.targetPropertyId.positive}")
    private Long targetPropertyId;

    @NotNull(message = "{validation.game.session.create.useMyData.notNull}")
    private Boolean useMyData;

    @Builder(access = AccessLevel.PRIVATE)
    private CreateGameSessionRequest(
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

    public static CreateGameSessionRequest of(
        final Integer slotNumber,
        final CharacterType characterType,
        final String characterName,
        final JobType jobType,
        final String regionCode,
        final String districtCode,
        final Long targetPropertyId,
        final Boolean useMyData
    ) {
        return CreateGameSessionRequest.builder()
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

    public CreateGameSessionServiceRequest toServiceRequest() {
        return CreateGameSessionServiceRequest.of(
            slotNumber,
            characterType,
            characterName,
            jobType,
            regionCode,
            districtCode,
            targetPropertyId,
            useMyData
        );
    }
}
