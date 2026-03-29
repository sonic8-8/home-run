package io.ssafy.p.j14c103.homerun.api.controller.game.career;

import io.ssafy.p.j14c103.homerun.api.controller.game.career.request.AcceptJobTransferRequest;
import io.ssafy.p.j14c103.homerun.api.service.game.career.GameCareerService;
import io.ssafy.p.j14c103.homerun.api.service.game.career.response.JobOfferListResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.career.response.JobTransferResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.career.response.SalaryNegotiationResponse;
import io.ssafy.p.j14c103.homerun.domain.user.auth.AuthenticatedUser;
import io.ssafy.p.j14c103.homerun.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games/sessions/{sessionId}/career")
@RequiredArgsConstructor
public class GameCareerController {

    private final GameCareerService gameCareerService;

    @PostMapping("/negotiate")
    public ApiResponse<SalaryNegotiationResponse> negotiateSalary(
        @AuthenticationPrincipal final AuthenticatedUser authenticatedUser,
        @PathVariable final Long sessionId
    ) {
        final SalaryNegotiationResponse response = gameCareerService.negotiateSalary(
            authenticatedUser.getUserId(),
            sessionId
        );
        return ApiResponse.ok(response);
    }

    @GetMapping("/job-offers")
    public ApiResponse<JobOfferListResponse> getJobOffers(
        @AuthenticationPrincipal final AuthenticatedUser authenticatedUser,
        @PathVariable final Long sessionId
    ) {
        final JobOfferListResponse response = gameCareerService.getJobOffers(
            authenticatedUser.getUserId(),
            sessionId
        );
        return ApiResponse.ok(response);
    }

    @PostMapping("/transfer")
    public ApiResponse<JobTransferResponse> transfer(
        @AuthenticationPrincipal final AuthenticatedUser authenticatedUser,
        @PathVariable final Long sessionId,
        @RequestBody final AcceptJobTransferRequest request
    ) {
        final JobTransferResponse response = gameCareerService.transfer(
            authenticatedUser.getUserId(),
            sessionId,
            request.toServiceRequest()
        );
        return ApiResponse.ok(response);
    }
}
