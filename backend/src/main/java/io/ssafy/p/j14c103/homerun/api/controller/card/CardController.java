package io.ssafy.p.j14c103.homerun.api.controller.card;

import io.ssafy.p.j14c103.homerun.api.service.card.CardService;
import io.ssafy.p.j14c103.homerun.api.service.card.response.CardListResponse;
import io.ssafy.p.j14c103.homerun.api.service.card.response.CardRecommendationResponse;
import io.ssafy.p.j14c103.homerun.api.service.card.response.CardTransactionListResponse;
import io.ssafy.p.j14c103.homerun.api.service.card.response.OwnedCardListResponse;
import io.ssafy.p.j14c103.homerun.domain.user.auth.AuthenticatedUser;
import io.ssafy.p.j14c103.homerun.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

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

    @GetMapping("/me")
    public ApiResponse<OwnedCardListResponse> getMyCards(
            @AuthenticationPrincipal final AuthenticatedUser authenticatedUser
    ) {
        final OwnedCardListResponse response = cardService.getMyCards(authenticatedUser.getUserId());
        return ApiResponse.ok(response);
    }

    @GetMapping("/me/transactions")
    public ApiResponse<CardTransactionListResponse> getMyTransactions(
            @AuthenticationPrincipal final AuthenticatedUser authenticatedUser
    ) {
        final CardTransactionListResponse response = cardService.getMyTransactions(authenticatedUser.getUserId());
        return ApiResponse.ok(response);
    }
}
