package io.ssafy.p.j14c103.homerun.api.controller.user;

import io.ssafy.p.j14c103.homerun.api.service.user.UserAssetLinkService;
import io.ssafy.p.j14c103.homerun.api.service.user.UserMeService;
import io.ssafy.p.j14c103.homerun.api.service.user.response.UserAssetLinkResponse;
import io.ssafy.p.j14c103.homerun.api.service.user.response.UserMeResponse;
import io.ssafy.p.j14c103.homerun.domain.user.auth.AuthenticatedUser;
import io.ssafy.p.j14c103.homerun.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserMeService userMeService;
    private final UserAssetLinkService userAssetLinkService;

    @GetMapping("/me")
    public ApiResponse<UserMeResponse> getMe(
            @AuthenticationPrincipal final AuthenticatedUser authenticatedUser
    ) {
        final UserMeResponse response = userMeService.getMe(authenticatedUser.getUserId());
        return ApiResponse.ok(response);
    }

    @PostMapping("/me/asset-link")
    public ApiResponse<UserAssetLinkResponse> linkAssets(
            @AuthenticationPrincipal final AuthenticatedUser authenticatedUser
    ) {
        final Long userId = authenticatedUser != null ? authenticatedUser.getUserId() : null;
        log.info("내 자산 연동 API 호출. userId={}", userId);

        try {
            final UserAssetLinkResponse response = userAssetLinkService.linkAssets(userId);
            log.info(
                    "내 자산 연동 API 응답. userId={}, isAssetLinked={}, mainAccountCreated={}, seedmoneyAccountCreated={}, summaryInitialized={}",
                    userId,
                    response.isAssetLinked(),
                    response.isMainAccountCreated(),
                    response.isSeedmoneyAccountCreated(),
                    response.isSummaryInitialized()
            );
            return ApiResponse.ok(response);
        } catch (final RuntimeException exception) {
            log.error("내 자산 연동 API 실패. userId={}", userId, exception);
            throw exception;
        }
    }
}
