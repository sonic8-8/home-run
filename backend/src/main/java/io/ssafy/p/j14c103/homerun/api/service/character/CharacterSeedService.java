package io.ssafy.p.j14c103.homerun.api.service.character;

import io.ssafy.p.j14c103.homerun.api.service.character.request.CharacterSeedRequest;
import io.ssafy.p.j14c103.homerun.api.service.character.response.CharacterSeedResponse;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterSeedPolicy;
import io.ssafy.p.j14c103.homerun.domain.character.SeedType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import org.springframework.stereotype.Service;

@Service
public class CharacterSeedService {

    private final CharacterSeedPolicy characterSeedPolicy = new CharacterSeedPolicy();

    public CharacterSeedResponse generate(final CharacterSeedRequest request) {
        if (request == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }

        final SeedType seedType = SeedType.from(request.getSeedType());
        return CharacterSeedResponse.from(
            characterSeedPolicy.calculate(
                request.getCharacterType(),
                request.getJobType(),
                seedType
            )
        );
    }
}
