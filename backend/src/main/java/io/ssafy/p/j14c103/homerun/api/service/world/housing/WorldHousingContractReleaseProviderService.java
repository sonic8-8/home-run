package io.ssafy.p.j14c103.homerun.api.service.world.housing;

import io.ssafy.p.j14c103.homerun.api.service.world.housing.response.HousingContractReleaseProviderResponse;
import io.ssafy.p.j14c103.homerun.domain.gamesession.housing.GameHousing;
import io.ssafy.p.j14c103.homerun.domain.gamesession.housing.GameHousingRepository;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WorldHousingContractReleaseProviderService {

    private final GameHousingRepository gameHousingRepository;

    public HousingContractReleaseProviderResponse getReleaseCriteria(final Long gameSessionId) {
        validateGameSessionId(gameSessionId);

        final GameHousing gameHousing = gameHousingRepository.findByGameSessionId(gameSessionId)
            .orElseThrow(() -> new HomerunException(ErrorCode.HOUSING_CURRENT_CONTRACT_NOT_FOUND));
        final HousingType previousHousingType = requireCurrentHousingType(gameHousing);
        final Money refundedDeposit = resolveRefundedDeposit(gameHousing, previousHousingType);

        return HousingContractReleaseProviderResponse.of(
            previousHousingType,
            HousingType.NONE,
            refundedDeposit,
            gameHousing.getCurrentPropertyId()
        );
    }

    private void validateGameSessionId(final Long gameSessionId) {
        if (gameSessionId == null || gameSessionId <= 0) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private HousingType requireCurrentHousingType(final GameHousing gameHousing) {
        final HousingType currentHousingType = gameHousing.getCurrentHousingType();
        if (currentHousingType == null || currentHousingType == HousingType.NONE) {
            throw new HomerunException(ErrorCode.HOUSING_CURRENT_CONTRACT_NOT_FOUND);
        }
        return currentHousingType;
    }

    private Money resolveRefundedDeposit(
        final GameHousing gameHousing,
        final HousingType currentHousingType
    ) {
        if (currentHousingType == HousingType.OWNED_APT) {
            requireCurrentPropertyId(gameHousing);
            return Money.zero();
        }

        final Money currentDeposit = gameHousing.getCurrentDeposit();
        if (currentDeposit == null) {
            throw new HomerunException(ErrorCode.WORLD_RESULT_INVALID);
        }
        return currentDeposit;
    }

    private void requireCurrentPropertyId(final GameHousing gameHousing) {
        if (gameHousing.getCurrentPropertyId() == null) {
            throw new HomerunException(ErrorCode.WORLD_RESULT_INVALID);
        }
    }
}
