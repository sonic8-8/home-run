package io.ssafy.p.j14c103.homerun.api.controller.character;

import io.ssafy.p.j14c103.homerun.api.service.character.CharacterQueryService;
import io.ssafy.p.j14c103.homerun.api.service.character.response.CharacterOptionsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games")
public class CharacterController {

    private final CharacterQueryService characterQueryService;

    public CharacterController(final CharacterQueryService characterQueryService) {
        this.characterQueryService = characterQueryService;
    }

    @GetMapping("/characters")
    public ResponseEntity<CharacterOptionsResponse> getCharacters() {
        final CharacterOptionsResponse response = characterQueryService.getCharacterOptions();
        return ResponseEntity.ok(response);
    }
}
