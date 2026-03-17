package io.ssafy.p.j14c103.homerun.api.service.character.response;

import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import java.util.List;

public record CharacterOptionsResponse(
    List<CharacterOptionResponse> characters
) {

    public CharacterOptionsResponse {
        if (characters == null) {
            throw new IllegalArgumentException("characters는 null일 수 없습니다.");
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
                throw new IllegalArgumentException("characterType은 null일 수 없습니다.");
            }
            if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
                throw new IllegalArgumentException("thumbnailUrl은 비어 있을 수 없습니다.");
            }
        }

        public static CharacterOptionResponse of(
            final CharacterType characterType,
            final String thumbnailUrl
        ) {
            return new CharacterOptionResponse(characterType, thumbnailUrl);
        }
    }
}
