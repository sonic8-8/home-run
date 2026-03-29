package io.ssafy.p.j14c103.homerun.api.service.card;

import io.ssafy.p.j14c103.homerun.api.service.image.ImageStorageService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardImageUrlResolver {

    private final Optional<ImageStorageService> imageStorageService;

    public String resolve(final String cardImageUrl) {
        if (cardImageUrl == null || cardImageUrl.isBlank()) {
            return "";
        }
        if (isDirectImageUrl(cardImageUrl)) {
            return cardImageUrl;
        }

        final ImageStorageService service = imageStorageService.orElse(null);
        if (service == null) {
            return "";
        }

        final String publicUrl = service.getPublicUrl(cardImageUrl);
        if (publicUrl == null || publicUrl.isBlank()) {
            return "";
        }

        return publicUrl;
    }

    private boolean isDirectImageUrl(final String cardImageUrl) {
        if (cardImageUrl.startsWith("http://")) {
            return true;
        }
        if (cardImageUrl.startsWith("https://")) {
            return true;
        }

        return cardImageUrl.startsWith("data:");
    }
}
