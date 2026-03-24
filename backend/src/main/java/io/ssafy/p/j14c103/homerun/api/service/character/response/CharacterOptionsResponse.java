package io.ssafy.p.j14c103.homerun.api.service.character.response;

import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CharacterOptionsResponse {

    private final List<CharacterOptionResponse> characters;

    @Builder(access = AccessLevel.PRIVATE)
    private CharacterOptionsResponse(final List<CharacterOptionResponse> characters) {
        validateRequest(characters);

        this.characters = List.copyOf(characters);
    }

    public static CharacterOptionsResponse from(final List<CharacterOptionResponse> characters) {
        return CharacterOptionsResponse.builder()
            .characters(characters)
            .build();
    }

    private void validateRequest(final List<CharacterOptionResponse> characters) {
        if (characters == null) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
    }

    @Getter
    public static class CharacterOptionResponse {

        private final CharacterType characterType;
        private final String thumbnailUrl;

        @Builder(access = AccessLevel.PRIVATE)
        private CharacterOptionResponse(
            final CharacterType characterType,
            final String thumbnailUrl
        ) {
            validateRequest(characterType, thumbnailUrl);

            this.characterType = characterType;
            this.thumbnailUrl = thumbnailUrl;
        }

        public static CharacterOptionResponse of(
            final CharacterType characterType,
            final String thumbnailUrl
        ) {
            return CharacterOptionResponse.builder()
                .characterType(characterType)
                .thumbnailUrl(thumbnailUrl)
                .build();
        }

        private void validateRequest(
            final CharacterType characterType,
            final String thumbnailUrl
        ) {
            if (characterType == null) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }
            if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }
        }
    }
}
