package io.ssafy.p.j14c103.homerun.api.controller.auth;

import io.ssafy.p.j14c103.homerun.api.controller.auth.request.LoginRequest;
import io.ssafy.p.j14c103.homerun.api.controller.auth.request.SignupRequest;
import io.ssafy.p.j14c103.homerun.api.service.auth.LoginService;
import io.ssafy.p.j14c103.homerun.api.service.auth.RefreshAccessTokenService;
import io.ssafy.p.j14c103.homerun.api.service.auth.SignupService;
import io.ssafy.p.j14c103.homerun.api.service.auth.request.RefreshAccessTokenServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.auth.response.LoginResponse;
import io.ssafy.p.j14c103.homerun.api.service.auth.response.RefreshAccessTokenResponse;
import io.ssafy.p.j14c103.homerun.api.service.auth.response.SignupResponse;
import io.ssafy.p.j14c103.homerun.global.ApiResponse;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final SignupService signupService;
    private final LoginService loginService;
    private final RefreshAccessTokenService refreshAccessTokenService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = signupService.signup(request.toServiceRequest());

        return ApiResponse.created(response);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = loginService.login(request.toServiceRequest());

        return ApiResponse.ok(response);
    }

    @PostMapping("/refresh")
    public ApiResponse<RefreshAccessTokenResponse> refresh(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader
    ) {
        RefreshAccessTokenResponse response = refreshAccessTokenService.refresh(
                RefreshAccessTokenServiceRequest.of(extractRefreshToken(authorizationHeader))
        );

        return ApiResponse.ok(response);
    }

    private String extractRefreshToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw HomerunException.from(ErrorCode.AUTH_REFRESH_INVALID);
        }
        if (!authorizationHeader.startsWith("Bearer ")) {
            throw HomerunException.from(ErrorCode.AUTH_REFRESH_INVALID);
        }

        String refreshToken = authorizationHeader.substring("Bearer ".length()).trim();

        if (refreshToken.isBlank()) {
            throw HomerunException.from(ErrorCode.AUTH_REFRESH_INVALID);
        }

        return refreshToken;
    }
}
