package io.ssafy.p.j14c103.homerun.api.controller.game.start;

import io.ssafy.p.j14c103.homerun.api.service.game.start.GameStartProfileService;
import io.ssafy.p.j14c103.homerun.api.service.game.start.response.ProfileOptionsResponse;
import io.ssafy.p.j14c103.homerun.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameStartProfileController {

    private final GameStartProfileService gameStartProfileService;

    @GetMapping("/profiles")
    public ApiResponse<ProfileOptionsResponse> getProfiles() {
        final ProfileOptionsResponse response = gameStartProfileService.getProfileOptions();
        return ApiResponse.ok(response);
    }
}
