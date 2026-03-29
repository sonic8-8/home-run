package io.ssafy.p.j14c103.homerun.api.controller.game.realestate;

import io.ssafy.p.j14c103.homerun.api.controller.game.realestate.request.SubmitContractReviewRequest;
import io.ssafy.p.j14c103.homerun.api.service.game.realestate.RealEstateContractReviewService;
import io.ssafy.p.j14c103.homerun.api.service.game.realestate.response.ContractReviewResponse;
import io.ssafy.p.j14c103.homerun.domain.user.auth.AuthenticatedUser;
import io.ssafy.p.j14c103.homerun.global.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/games/sessions/{sessionId}/real-estate/properties/{propertyId}/contract")
public class RealEstateContractReviewController {

    private final RealEstateContractReviewService realEstateContractReviewService;

    @PostMapping
    public ApiResponse<ContractReviewResponse> reviewContract(
            @AuthenticationPrincipal final AuthenticatedUser authenticatedUser,
            @PathVariable final Long sessionId,
            @PathVariable final Long propertyId,
            @Valid @RequestBody final SubmitContractReviewRequest request
    ) {
        final ContractReviewResponse response = realEstateContractReviewService.review(
                authenticatedUser.getUserId(),
                sessionId,
                propertyId,
                request.toServiceRequest()
        );
        return ApiResponse.ok(response);
    }
}
