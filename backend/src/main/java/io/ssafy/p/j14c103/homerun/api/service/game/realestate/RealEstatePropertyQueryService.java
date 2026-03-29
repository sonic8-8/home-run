package io.ssafy.p.j14c103.homerun.api.service.game.realestate;

import io.ssafy.p.j14c103.homerun.api.service.game.realestate.response.RealEstatePropertyDetailResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.realestate.response.RealEstatePropertyListResponse;
import io.ssafy.p.j14c103.homerun.api.service.user.UserAuthContextService;
import io.ssafy.p.j14c103.homerun.api.service.world.TargetPropertyProviderService;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.WorldRealEstatePropertyProviderService;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.request.RealEstatePropertyBoundsServiceRequest;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RealEstatePropertyQueryService {

    private static final int BOUNDS_TOKEN_COUNT = 4;

    private final GameSessionRepository gameSessionRepository;
    private final UserAuthContextService userAuthContextService;
    private final WorldRealEstatePropertyProviderService worldRealEstatePropertyProviderService;
    private final TargetPropertyProviderService targetPropertyProviderService;

    public RealEstatePropertyListResponse getProperties(
        final Long userId,
        final Long sessionId,
        final String bounds
    ) {
        userAuthContextService.getContext(userId);
        final GameSession gameSession = getOwnedGameSession(userId, sessionId);

        if (!StringUtils.hasText(bounds)) {
            return RealEstatePropertyListResponse.from(
                targetPropertyProviderService.getTargetProperties(
                    gameSession.getRegionCode(),
                    gameSession.getDistrictCode()
                )
            );
        }

        return RealEstatePropertyListResponse.from(
            worldRealEstatePropertyProviderService.getPropertiesInBounds(parseBounds(bounds))
        );
    }

    public RealEstatePropertyDetailResponse getPropertyDetail(
        final Long userId,
        final Long sessionId,
        final Long propertyId
    ) {
        userAuthContextService.getContext(userId);
        getOwnedGameSession(userId, sessionId);

        return RealEstatePropertyDetailResponse.from(
            worldRealEstatePropertyProviderService.getPropertyDetail(propertyId)
        );
    }

    private GameSession getOwnedGameSession(final Long userId, final Long sessionId) {
        final GameSession gameSession = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new HomerunException(ErrorCode.GAME_SESSION_NOT_FOUND));
        gameSession.assertOwner(userId);
        return gameSession;
    }

    private RealEstatePropertyBoundsServiceRequest parseBounds(final String bounds) {
        final List<String> tokens = Arrays.stream(bounds.split(","))
            .map(String::trim)
            .toList();

        if (tokens.size() != BOUNDS_TOKEN_COUNT) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }

        final BigDecimal minLongitude = parseCoordinate(tokens.get(0));
        final BigDecimal minLatitude = parseCoordinate(tokens.get(1));
        final BigDecimal maxLongitude = parseCoordinate(tokens.get(2));
        final BigDecimal maxLatitude = parseCoordinate(tokens.get(3));

        return RealEstatePropertyBoundsServiceRequest.of(
            minLatitude,
            minLongitude,
            maxLatitude,
            maxLongitude
        );
    }

    private BigDecimal parseCoordinate(final String value) {
        try {
            return new BigDecimal(value);
        } catch (final NumberFormatException exception) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
