package io.ssafy.p.j14c103.homerun.api.service.character;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import io.ssafy.p.j14c103.homerun.api.service.character.response.CharacterOptionsResponse;
import io.ssafy.p.j14c103.homerun.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CharacterQueryServiceTest extends IntegrationTestSupport {

    @Autowired
    private CharacterQueryService characterQueryService;

    @DisplayName("캐릭터 선택지 메타데이터를 반환한다.")
    @Test
    void getCharacterOptions() {
        // when
        final CharacterOptionsResponse response = characterQueryService.getCharacterOptions();

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
