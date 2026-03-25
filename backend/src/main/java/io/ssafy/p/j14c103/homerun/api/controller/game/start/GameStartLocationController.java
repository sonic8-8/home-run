package io.ssafy.p.j14c103.homerun.api.controller.game.start;

import io.ssafy.p.j14c103.homerun.api.service.game.start.GameStartLocationService;
import io.ssafy.p.j14c103.homerun.api.service.game.start.response.DistrictListResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.start.response.RegionListResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.start.response.TargetPropertyListResponse;
import io.ssafy.p.j14c103.homerun.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameStartLocationController {

    private final GameStartLocationService gameStartLocationService;

    @GetMapping("/regions")
    public ApiResponse<RegionListResponse> getRegions() {
        final RegionListResponse response = gameStartLocationService.getRegions();
        return ApiResponse.ok(response);
    }

    @GetMapping("/regions/{regionCode}/districts")
    public ApiResponse<DistrictListResponse> getDistricts(
        @PathVariable final String regionCode
    ) {
        final DistrictListResponse response = gameStartLocationService.getDistricts(regionCode);
        return ApiResponse.ok(response);
    }

    @GetMapping("/regions/{regionCode}/districts/{districtCode}/properties")
    public ApiResponse<TargetPropertyListResponse> getTargetProperties(
        @PathVariable final String regionCode,
        @PathVariable final String districtCode
    ) {
        final TargetPropertyListResponse response =
            gameStartLocationService.getTargetProperties(regionCode, districtCode);
        return ApiResponse.ok(response);
    }
}
