package io.ssafy.p.j14c103.homerun.api.service.character;

import io.ssafy.p.j14c103.homerun.api.service.character.history.GameplayHistoryWriter;
import io.ssafy.p.j14c103.homerun.api.service.character.request.CharacterTurnResultServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.character.response.CharacterTurnResultServiceResponse;
import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.domain.character.GameStatRepository;
import io.ssafy.p.j14c103.homerun.domain.character.HealthRisk;
import io.ssafy.p.j14c103.homerun.domain.character.StatAutoChangePolicy;
import io.ssafy.p.j14c103.homerun.domain.character.career.ForcedResignationPolicy;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareer;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareerRepository;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobTitlePolicy;
import io.ssafy.p.j14c103.homerun.domain.character.career.NegotiationPreparationPolicy;
import io.ssafy.p.j14c103.homerun.domain.character.career.UnemploymentBenefitPolicy;
import io.ssafy.p.j14c103.homerun.domain.character.schedule.ActionType;
import io.ssafy.p.j14c103.homerun.domain.character.schedule.GameTurnSlot;
import io.ssafy.p.j14c103.homerun.domain.character.schedule.GameTurnSlotRepository;
import io.ssafy.p.j14c103.homerun.domain.character.schedule.TurnSlotPreviewPolicy;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CharacterTurnResultService {

    private final GameStatRepository gameStatRepository;
    private final GameCareerRepository gameCareerRepository;
    private final GameTurnSlotRepository gameTurnSlotRepository;
    private final GameplayHistoryWriter gameplayHistoryWriter;

    private final TurnSlotPreviewPolicy turnSlotPreviewPolicy = new TurnSlotPreviewPolicy();
    private final StatAutoChangePolicy statAutoChangePolicy = new StatAutoChangePolicy();
    private final JobTitlePolicy jobTitlePolicy = new JobTitlePolicy();
    private final NegotiationPreparationPolicy negotiationPreparationPolicy =
        new NegotiationPreparationPolicy();
    private final ForcedResignationPolicy forcedResignationPolicy =
        new ForcedResignationPolicy();
    private final UnemploymentBenefitPolicy unemploymentBenefitPolicy =
        new UnemploymentBenefitPolicy();

    public CharacterTurnResultServiceResponse apply(
        final CharacterTurnResultServiceRequest request
    ) {
        validateRequest(request);

        final GameStat gameStat = request.getGameStat();
        final GameCareer gameCareer = request.getGameCareer();
        final GameplayHistoryWriter.StatSnapshot previousStat =
            GameplayHistoryWriter.StatSnapshot.from(gameStat);

        applyTurnActionResult(gameStat, request);
        applyAutoChange(gameStat, request);
        applyNegotiationPreparation(gameCareer, gameStat, request);
        gameCareer.advanceTurn(jobTitlePolicy, request.getCurrentTurn());

        final HealthRisk healthRisk = gameStat.evaluateHealthRisk();
        final boolean forcedResigned = applyForcedResignationIfNeeded(
            gameCareer,
            gameStat,
            request.getCurrentTurn()
        );
        final UnemploymentBenefitOutcome unemploymentBenefitOutcome =
            consumeUnemploymentBenefit(gameCareer);

        gameStatRepository.save(gameStat);
        gameCareerRepository.save(gameCareer);
        gameplayHistoryWriter.writeStatChange(previousStat, gameStat, request.getCurrentTurn());

        return CharacterTurnResultServiceResponse.of(
            healthRisk,
            forcedResigned,
            unemploymentBenefitOutcome.isGranted(),
            unemploymentBenefitOutcome.getAmount(),
            gameStat,
            gameCareer
        );
    }

    private void applyTurnActionResult(
        final GameStat gameStat,
        final CharacterTurnResultServiceRequest request
    ) {
        final TurnSlotPreviewPolicy.PreviewResult previewResult = turnSlotPreviewPolicy.preview(
            gameStat,
            request.toRequestedSlots()
        );
        final TurnSlotPreviewPolicy.StatPreview statPreview = previewResult.statPreview();

        gameStat.applyChange(
            statPreview.healthDelta(),
            statPreview.fatigueDelta(),
            statPreview.stressDelta(),
            statPreview.happinessDelta(),
            statPreview.knowledgeDelta(),
            request.getCurrentTurn()
        );
    }

    private void applyAutoChange(
        final GameStat gameStat,
        final CharacterTurnResultServiceRequest request
    ) {
        final StatAutoChangePolicy.StatAutoChange autoChange = statAutoChangePolicy.calculate(
            gameStat,
            request.getHousingType()
        );

        gameStat.applyChange(
            autoChange.healthDelta(),
            autoChange.fatigueDelta(),
            autoChange.stressDelta(),
            autoChange.happinessDelta(),
            autoChange.knowledgeDelta(),
            request.getCurrentTurn()
        );
    }

    private void applyNegotiationPreparation(
        final GameCareer gameCareer,
        final GameStat gameStat,
        final CharacterTurnResultServiceRequest request
    ) {
        final NegotiationPreparationPolicy.PreparationResult preparationResult =
            negotiationPreparationPolicy.calculate(
                gameCareer,
                gameStat,
                request.getTurnActions()
                    .stream()
                    .map(CharacterTurnResultServiceRequest.TurnActionRequest::getActionType)
                    .toList(),
                loadExpiredTurnActions(gameCareer.getGameId(), request.getCurrentTurn())
            );

        gameCareer.applyNegotiationPreparation(preparationResult);
    }

    private List<ActionType> loadExpiredTurnActions(final Integer gameId, final int currentTurn) {
        if (currentTurn <= 12) {
            return List.of();
        }

        return gameTurnSlotRepository.findAllByGameIdAndTurnNumberOrderBySlotIndex(
            gameId,
            currentTurn - 12
        )
            .stream()
            .map(GameTurnSlot::getActionType)
            .toList();
    }

    private boolean applyForcedResignationIfNeeded(
        final GameCareer gameCareer,
        final GameStat gameStat,
        final int currentTurn
    ) {
        if (!gameStat.isForcedResignationRisk()) {
            return false;
        }
        if (gameCareer.getEmploymentStatus() == EmploymentStatus.UNEMPLOYED) {
            return false;
        }

        final GameplayHistoryWriter.CareerSnapshot beforeResignation =
            GameplayHistoryWriter.CareerSnapshot.from(gameCareer);
        final ForcedResignationPolicy.ForcedResignationResult forcedResignationResult =
            forcedResignationPolicy.apply(gameCareer, gameStat, currentTurn);

        gameCareer.forceResign(forcedResignationResult);
        gameplayHistoryWriter.writeForcedResignation(beforeResignation, gameCareer, currentTurn);
        return true;
    }

    private UnemploymentBenefitOutcome consumeUnemploymentBenefit(
        final GameCareer gameCareer
    ) {
        final UnemploymentBenefitPolicy.UnemploymentBenefitResult benefitResult =
            unemploymentBenefitPolicy.calculate(gameCareer);
        if (!benefitResult.benefitGranted()) {
            return UnemploymentBenefitOutcome.none();
        }

        gameCareer.consumeUnemploymentBenefit();
        return UnemploymentBenefitOutcome.of(benefitResult.benefitAmount());
    }

    private void validateRequest(final CharacterTurnResultServiceRequest request) {
        if (request == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
    }

    private static final class UnemploymentBenefitOutcome {

        private final boolean granted;
        private final int amount;

        private UnemploymentBenefitOutcome(final boolean granted, final int amount) {
            this.granted = granted;
            this.amount = amount;
        }

        private static UnemploymentBenefitOutcome of(final int amount) {
            return new UnemploymentBenefitOutcome(true, amount);
        }

        private static UnemploymentBenefitOutcome none() {
            return new UnemploymentBenefitOutcome(false, 0);
        }

        private boolean isGranted() {
            return granted;
        }

        private int getAmount() {
            return amount;
        }
    }
}
