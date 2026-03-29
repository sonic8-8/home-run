package io.ssafy.p.j14c103.homerun.api.controller.game.realestate;

import io.ssafy.p.j14c103.homerun.api.service.game.realestate.RealEstatePropertyQueryService;
import io.ssafy.p.j14c103.homerun.api.service.game.realestate.response.RealEstatePropertyDetailResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.realestate.response.RealEstatePropertyListResponse;
import io.ssafy.p.j14c103.homerun.domain.user.auth.AuthenticatedUser;
import io.ssafy.p.j14c103.homerun.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/games/sessions/{sessionId}/real-estate/properties")
public class RealEstatePropertyController {

    private final RealEstatePropertyQueryService realEstatePropertyQueryService;

    @GetMapping
    public ApiResponse<RealEstatePropertyListResponse> getProperties(
        @AuthenticationPrincipal final AuthenticatedUser authenticatedUser,
        @PathVariable final Long sessionId,
        @RequestParam(required = false) final String bounds
    ) {
        final RealEstatePropertyListResponse response = realEstatePropertyQueryService.getProperties(
            authenticatedUser.getUserId(),
            sessionId,
            bounds
        );
        return ApiResponse.ok(response);
    }

    @GetMapping("/{propertyId}")
    public ApiResponse<RealEstatePropertyDetailResponse> getPropertyDetail(
        @AuthenticationPrincipal final AuthenticatedUser authenticatedUser,
        @PathVariable final Long sessionId,
        @PathVariable final Long propertyId
    ) {
        final RealEstatePropertyDetailResponse response = realEstatePropertyQueryService.getPropertyDetail(
            authenticatedUser.getUserId(),
            sessionId,
            propertyId
        );
        return ApiResponse.ok(response);
    }
}
