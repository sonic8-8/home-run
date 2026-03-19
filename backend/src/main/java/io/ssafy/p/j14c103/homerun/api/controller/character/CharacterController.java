package io.ssafy.p.j14c103.homerun.api.controller.character;

import io.ssafy.p.j14c103.homerun.api.service.character.CharacterQueryService;
import io.ssafy.p.j14c103.homerun.api.service.character.response.CharacterOptionsResponse;
import io.ssafy.p.j14c103.homerun.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class CharacterController {

    private final CharacterQueryService characterQueryService;

    @GetMapping("/characters")
    public ApiResponse<CharacterOptionsResponse> getCharacters() {
        final CharacterOptionsResponse response = characterQueryService.getCharacterOptions();
        return ApiResponse.ok(response);
    }
}
