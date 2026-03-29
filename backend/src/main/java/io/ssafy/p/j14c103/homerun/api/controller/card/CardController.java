package io.ssafy.p.j14c103.homerun.api.controller.card;

import io.ssafy.p.j14c103.homerun.api.controller.card.request.CardApplyRequest;
import io.ssafy.p.j14c103.homerun.api.service.card.CardService;
import io.ssafy.p.j14c103.homerun.api.service.card.OwnedCardService;
import io.ssafy.p.j14c103.homerun.api.service.card.response.CardListResponse;
import io.ssafy.p.j14c103.homerun.api.service.card.response.CardRecommendationResponse;
import io.ssafy.p.j14c103.homerun.api.service.card.response.OwnedCardListResponse;
import io.ssafy.p.j14c103.homerun.api.service.card.response.OwnedCardResponse;
import io.ssafy.p.j14c103.homerun.domain.user.auth.AuthenticatedUser;
import io.ssafy.p.j14c103.homerun.global.ApiResponse;
import jakarta.validation.Valid;
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
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;
    private final OwnedCardService ownedCardService;

    @GetMapping
    public ApiResponse<CardListResponse> getCards() {
        final CardListResponse response = cardService.getCards();
        return ApiResponse.ok(response);
    }

    @GetMapping("/recommendations")
    public ApiResponse<CardRecommendationResponse> getRecommendations(
            @AuthenticationPrincipal final AuthenticatedUser authenticatedUser
    ) {
        final CardRecommendationResponse response = cardService.getRecommendations(authenticatedUser.getUserId());
        return ApiResponse.ok(response);
    }

    @GetMapping("/recommendations/v2")
    public ApiResponse<CardRecommendationResponse> getPreferenceRecommendations(
            @AuthenticationPrincipal final AuthenticatedUser authenticatedUser
    ) {
        final CardRecommendationResponse response = cardService
                .getPreferenceRecommendations(authenticatedUser.getUserId());
        return ApiResponse.ok(response);
    }

    @GetMapping("/owned")
    public ApiResponse<OwnedCardListResponse> getOwnedCards(
            @AuthenticationPrincipal final AuthenticatedUser authenticatedUser
    ) {
        final OwnedCardListResponse response = ownedCardService.getOwnedCards(authenticatedUser.getUserId());
        return ApiResponse.ok(response);
    }

    @PostMapping("/apply")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OwnedCardResponse> applyCard(
            @AuthenticationPrincipal final AuthenticatedUser authenticatedUser,
            @Valid @RequestBody final CardApplyRequest request
    ) {
        final OwnedCardResponse response = ownedCardService.applyCard(
                authenticatedUser.getUserId(),
                request.toServiceRequest()
        );
        return ApiResponse.created(response);
    }

    @DeleteMapping("/owned/{ownedCardId}")
    public ResponseEntity<Void> cancelOwnedCard(
            @AuthenticationPrincipal final AuthenticatedUser authenticatedUser,
            @PathVariable final Long ownedCardId
    ) {
        ownedCardService.cancelOwnedCard(authenticatedUser.getUserId(), ownedCardId);
        return ResponseEntity.noContent().build();
    }
}
