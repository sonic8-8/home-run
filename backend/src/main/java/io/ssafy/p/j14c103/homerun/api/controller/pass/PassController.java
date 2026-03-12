package io.ssafy.p.j14c103.homerun.api.controller.pass;

import io.ssafy.p.j14c103.homerun.api.dto.pass.PassProductResponse;
import io.ssafy.p.j14c103.homerun.api.dto.pass.PassSubscribeRequest;
import io.ssafy.p.j14c103.homerun.api.dto.pass.PassSubscriptionResponse;
import io.ssafy.p.j14c103.homerun.api.service.pass.PassService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pass")
public class PassController {

    private final PassService passService;

    public PassController(final PassService passService) {
        this.passService = passService;
    }

    @GetMapping("/products")
    public ResponseEntity<List<PassProductResponse>> getProducts() {
        final List<PassProductResponse> response = passService.getProducts();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<List<PassSubscriptionResponse>> getSubscriptions(
            @RequestParam final Long userId) {
        final List<PassSubscriptionResponse> response = passService.getSubscriptions(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/subscribe")
    public ResponseEntity<PassSubscriptionResponse> subscribe(
            @Valid @RequestBody final PassSubscribeRequest request) {
        final PassSubscriptionResponse response = passService.subscribe(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/subscriptions/{id}")
    public ResponseEntity<Void> cancelSubscription(@PathVariable final Long id) {
        passService.cancelSubscription(id);
        return ResponseEntity.noContent().build();
    }
}
