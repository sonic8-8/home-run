package io.ssafy.p.j14c103.homerun.api.controller.pass;

import io.ssafy.p.j14c103.homerun.api.controller.pass.request.PassSaveRequest;
import io.ssafy.p.j14c103.homerun.api.service.pass.PassSavingService;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassHistoryResponse;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassSaveResponse;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassWidgetResponse;
import io.ssafy.p.j14c103.homerun.domain.user.auth.AuthenticatedUser;
import io.ssafy.p.j14c103.homerun.global.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pass")
@RequiredArgsConstructor
public class PassSavingController {

    private final PassSavingService passSavingService;

    @PostMapping("/save")
    public ApiResponse<PassSaveResponse> save(
            @AuthenticationPrincipal final AuthenticatedUser authenticatedUser,
            @Valid @RequestBody final PassSaveRequest request) {
        final PassSaveResponse response = passSavingService.save(
                authenticatedUser.getUserId(),
                request.toServiceRequest());
        return ApiResponse.ok(response);
    }

    @GetMapping("/widget")
    public ApiResponse<PassWidgetResponse> getWidget(
            @AuthenticationPrincipal final AuthenticatedUser authenticatedUser) {
        final PassWidgetResponse response = passSavingService.getWidget(authenticatedUser.getUserId());
        return ApiResponse.ok(response);
    }

    @GetMapping("/history")
    public ApiResponse<Page<PassHistoryResponse>> getHistory(
            @AuthenticationPrincipal final AuthenticatedUser authenticatedUser,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "20") final int size) {
        final Page<PassHistoryResponse> response = passSavingService.getHistory(authenticatedUser.getUserId(), page, size);
        return ApiResponse.ok(response);
    }
}
