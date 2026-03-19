package io.ssafy.p.j14c103.homerun.api.controller.pass;

import io.ssafy.p.j14c103.homerun.api.controller.pass.request.PassSubscribeRequest;
import io.ssafy.p.j14c103.homerun.api.service.pass.PassService;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassProductResponse;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassSubscriptionResponse;
import io.ssafy.p.j14c103.homerun.domain.user.auth.AuthenticatedUser;
import io.ssafy.p.j14c103.homerun.global.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pass")
@RequiredArgsConstructor
public class PassController {

    private final PassService passService;

    @GetMapping("/products")
    public ApiResponse<Map<String, List<PassProductResponse>>> getProducts() {
        final List<PassProductResponse> response = passService.getProducts();
        return ApiResponse.ok(Map.of("products", response));
    }

    @GetMapping("/subscriptions")
    public ApiResponse<Map<String, List<PassSubscriptionResponse>>> getSubscriptions(
            @AuthenticationPrincipal final AuthenticatedUser authenticatedUser) {
        final List<PassSubscriptionResponse> response = passService.getSubscriptions(authenticatedUser.getUserId());
        return ApiResponse.ok(Map.of("subscriptions", response));
    }

    @PostMapping("/subscribe")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PassSubscriptionResponse> subscribe(
            @AuthenticationPrincipal final AuthenticatedUser authenticatedUser,
            @Valid @RequestBody final PassSubscribeRequest request) {
        final PassSubscriptionResponse response = passService.subscribe(
                authenticatedUser.getUserId(),
                request.toServiceRequest());
        return ApiResponse.created(response);
    }

    @DeleteMapping("/subscriptions/{id}")
    public ResponseEntity<Void> cancelSubscription(
            @AuthenticationPrincipal final AuthenticatedUser authenticatedUser,
            @PathVariable final Long id) {
        passService.cancelSubscription(authenticatedUser.getUserId(), id);
        return ResponseEntity.noContent().build();
    }
}
