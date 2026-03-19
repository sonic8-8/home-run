package io.ssafy.p.j14c103.homerun.api.service.character.response;

import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;

public record CharacterOptionsResponse(
    List<CharacterOptionResponse> characters
) {

    public CharacterOptionsResponse {
        if (characters == null) {
            throwResponseInvalid();
        }
        characters = List.copyOf(characters);
    }

    public static CharacterOptionsResponse from(final List<CharacterOptionResponse> characters) {
        return new CharacterOptionsResponse(characters);
    }

    public record CharacterOptionResponse(
        CharacterType characterType,
        String thumbnailUrl
    ) {

        public CharacterOptionResponse {
            if (characterType == null) {
                throwResponseInvalid();
            }
            if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
                throwResponseInvalid();
            }
        }

        public static CharacterOptionResponse of(
            final CharacterType characterType,
            final String thumbnailUrl
        ) {
            return new CharacterOptionResponse(characterType, thumbnailUrl);
        }
    }

    private static void throwResponseInvalid() {
        throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
    }
}
