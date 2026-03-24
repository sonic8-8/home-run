package io.ssafy.p.j14c103.homerun.api.service.character.career;

import io.ssafy.p.j14c103.homerun.api.service.character.career.request.SalaryNegotiationRequest;
import io.ssafy.p.j14c103.homerun.api.service.character.career.response.SalaryNegotiationResultResponse;
import io.ssafy.p.j14c103.homerun.api.service.character.history.GameplayHistoryWriter;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareer;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareerRepository;
import io.ssafy.p.j14c103.homerun.domain.character.career.SalaryNegotiationPolicy;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SalaryNegotiationService {

    private final GameCareerRepository gameCareerRepository;
    private final GameplayHistoryWriter gameplayHistoryWriter;
    private final SalaryNegotiationPolicy salaryNegotiationPolicy = new SalaryNegotiationPolicy();

    public SalaryNegotiationResultResponse negotiate(final SalaryNegotiationRequest request) {
        validateRequest(request);

        final GameCareer gameCareer = request.gameCareer();
        final GameplayHistoryWriter.CareerSnapshot previousCareer =
            GameplayHistoryWriter.CareerSnapshot.from(gameCareer);
        final SalaryNegotiationPolicy.NegotiationResult negotiationResult =
            salaryNegotiationPolicy.negotiate(
                gameCareer,
                request.gameStat(),
                request.currentTurn()
            );

        gameCareer.applySalaryNegotiation(negotiationResult);
        gameCareerRepository.save(gameCareer);
        gameplayHistoryWriter.writeSalaryNegotiation(
            previousCareer,
            gameCareer,
            negotiationResult
        );
        return SalaryNegotiationResultResponse.from(negotiationResult);
    }

    private void validateRequest(final SalaryNegotiationRequest request) {
        if (request == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
    }
}
