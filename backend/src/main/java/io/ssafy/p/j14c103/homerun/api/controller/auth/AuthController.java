package io.ssafy.p.j14c103.homerun.api.controller.auth;

import io.ssafy.p.j14c103.homerun.api.controller.auth.request.SignupRequest;
import io.ssafy.p.j14c103.homerun.api.service.auth.SignupService;
import io.ssafy.p.j14c103.homerun.api.service.auth.response.SignupResponse;
import io.ssafy.p.j14c103.homerun.global.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final SignupService signupService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = signupService.signup(request.toServiceRequest());

        return ApiResponse.created(response);
    }
}
