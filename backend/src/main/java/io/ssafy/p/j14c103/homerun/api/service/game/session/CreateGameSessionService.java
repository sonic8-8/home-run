package io.ssafy.p.j14c103.homerun.api.service.game.session;

import io.ssafy.p.j14c103.homerun.api.service.game.session.request.CreateGameSessionServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.game.session.response.CreateGameSessionResponse;
import io.ssafy.p.j14c103.homerun.api.service.user.UserAuthContextService;
import io.ssafy.p.j14c103.homerun.api.service.world.TargetPropertyValidationService;
import io.ssafy.p.j14c103.homerun.api.service.world.response.TargetPropertyValidationResponse;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CreateGameSessionService {

    private final GameSessionRepository gameSessionRepository;
    private final UserAuthContextService userAuthContextService;
    private final TargetPropertyValidationService targetPropertyValidationService;

    public CreateGameSessionResponse create(
        final Long userId,
        final CreateGameSessionServiceRequest request
    ) {
        userAuthContextService.getContext(userId);
        validateSlotConflict(userId, request.getSlotNumber());

        final TargetPropertyValidationResponse validationResponse =
            targetPropertyValidationService.validateTargetProperty(
                request.getRegionCode(),
                request.getDistrictCode(),
                request.getTargetPropertyId()
            );

        final GameSession gameSession = GameSession.create(
            userId,
            request.getSlotNumber(),
            request.getCharacterName(),
            request.getCharacterType(),
            request.getJobType(),
            validationResponse.getHousingType(),
            request.getRegionCode(),
            request.getDistrictCode(),
            validationResponse.getPropertyId(),
            request.toDataSourceType()
        );

        final GameSession saved = gameSessionRepository.saveAndFlush(gameSession);
        return CreateGameSessionResponse.from(saved);
    }

    private void validateSlotConflict(final Long userId, final Integer slotNumber) {
        if (!gameSessionRepository.existsByUserIdAndSlotNumber(userId, slotNumber)) {
            return;
        }
        throw new HomerunException(ErrorCode.GAME_SLOT_CONFLICT);
    }
}
