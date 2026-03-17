package io.ssafy.p.j14c103.homerun.api.service.character;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import io.ssafy.p.j14c103.homerun.api.service.character.response.CharacterOptionsResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CharacterQueryServiceTest {

    private final CharacterQueryService characterQueryService = new CharacterQueryService();

    @DisplayName("캐릭터 선택지 메타데이터를 반환한다.")
    @Test
    void getCharacterOptions() {
        // when
        CharacterOptionsResponse response = characterQueryService.getCharacterOptions();

        // then
        assertThat(response.characters())
            .extracting(
                CharacterOptionsResponse.CharacterOptionResponse::characterType,
                CharacterOptionsResponse.CharacterOptionResponse::thumbnailUrl
            )
            .containsExactly(
                tuple(io.ssafy.p.j14c103.homerun.domain.character.CharacterType.FEMALE, "/images/characters/female.png"),
                tuple(io.ssafy.p.j14c103.homerun.domain.character.CharacterType.MALE, "/images/characters/male.png")
            );
    }
}
