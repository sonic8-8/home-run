package io.ssafy.p.j14c103.homerun.api.service.character;

import io.ssafy.p.j14c103.homerun.api.service.character.response.CharacterOptionsResponse;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CharacterQueryService {

    private static final String FEMALE_THUMBNAIL_URL = "/images/characters/female.png";
    private static final String MALE_THUMBNAIL_URL = "/images/characters/male.png";

    public CharacterOptionsResponse getCharacterOptions() {
        return CharacterOptionsResponse.from(List.of(
            CharacterOptionsResponse.CharacterOptionResponse.of(CharacterType.FEMALE, FEMALE_THUMBNAIL_URL),
            CharacterOptionsResponse.CharacterOptionResponse.of(CharacterType.MALE, MALE_THUMBNAIL_URL)
        ));
    }
}
