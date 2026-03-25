package io.ssafy.p.j14c103.homerun.api.service.character.response;

import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import lombok.Getter;

@Getter
public class CharacterOptionsResponse {

    private final List<CharacterOptionResponse> characters;

    private CharacterOptionsResponse(final List<CharacterOptionResponse> characters) {
        if (characters == null) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
        this.characters = List.copyOf(characters);
    }

    public static CharacterOptionsResponse from(final List<CharacterOptionResponse> characters) {
        return new CharacterOptionsResponse(characters);
    }

    public List<CharacterOptionResponse> characters() {
        return characters;
    }

    @Getter
    public static class CharacterOptionResponse {

        private final CharacterType characterType;
        private final String thumbnailUrl;

        private CharacterOptionResponse(
            final CharacterType characterType,
            final String thumbnailUrl
        ) {
            if (characterType == null) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }
            if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }

            this.characterType = characterType;
            this.thumbnailUrl = thumbnailUrl;
        }

        public static CharacterOptionResponse of(
            final CharacterType characterType,
            final String thumbnailUrl
        ) {
            return new CharacterOptionResponse(characterType, thumbnailUrl);
        }

        public CharacterType characterType() {
            return characterType;
        }

        public String thumbnailUrl() {
            return thumbnailUrl;
        }
    }
}
