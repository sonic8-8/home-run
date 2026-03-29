package io.ssafy.p.j14c103.homerun.api.controller.game.realestate;

import io.ssafy.p.j14c103.homerun.api.service.game.realestate.RealEstateDocumentService;
import io.ssafy.p.j14c103.homerun.api.service.game.realestate.response.RealEstateDocumentResponse;
import io.ssafy.p.j14c103.homerun.domain.user.auth.AuthenticatedUser;
import io.ssafy.p.j14c103.homerun.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/games/sessions/{sessionId}/real-estate/properties")
public class RealEstateDocumentController {

    private final RealEstateDocumentService realEstateDocumentService;

    @GetMapping("/{propertyId}/documents")
    public ApiResponse<RealEstateDocumentResponse> getDocuments(
        @AuthenticationPrincipal final AuthenticatedUser authenticatedUser,
        @PathVariable final Long sessionId,
        @PathVariable final Long propertyId
    ) {
        final RealEstateDocumentResponse response = realEstateDocumentService.getDocument(
            authenticatedUser.getUserId(),
            sessionId,
            propertyId
        );
        return ApiResponse.ok(response);
    }
}
