package io.ssafy.p.j14c103.homerun.api.service.character.career.response;

import io.ssafy.p.j14c103.homerun.domain.character.career.JobTransferPolicy;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;

public record JobOfferQueryResponse(
    List<JobOfferResponse> offers,
    int offerChanceBonusRate,
    boolean meetFriendBonusApplied
) {

    public JobOfferQueryResponse {
        if (offers == null) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
        if (offers.stream().anyMatch(java.util.Objects::isNull)) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
        if (offerChanceBonusRate < 0) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
        offers = List.copyOf(offers);
    }

    public static JobOfferQueryResponse from(
        final JobTransferPolicy.JobOfferPool jobOfferPool
    ) {
        if (jobOfferPool == null) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }

        return new JobOfferQueryResponse(
            jobOfferPool.offers().stream()
                .map(JobOfferResponse::from)
                .toList(),
            jobOfferPool.offerChanceBonusRate(),
            jobOfferPool.meetFriendBonusApplied()
        );
    }

    public record JobOfferResponse(
        String offerId,
        JobType jobType,
        String displayCompanyName,
        int currentSalary,
        int offeredSalary,
        Integer probationTurns
    ) {

        public JobOfferResponse {
            if (offerId == null || offerId.isBlank()) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }
            if (jobType == null) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }
            if (displayCompanyName == null || displayCompanyName.isBlank()) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }
            if (currentSalary <= 0) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }
            if (offeredSalary <= 0) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }
            if (probationTurns != null && probationTurns <= 0) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }
        }

        private static JobOfferResponse from(final JobTransferPolicy.JobOffer jobOffer) {
            return new JobOfferResponse(
                jobOffer.offerId(),
                jobOffer.jobType(),
                jobOffer.displayCompanyName(),
                jobOffer.currentSalary(),
                jobOffer.offeredSalary(),
                jobOffer.probationTurns()
            );
        }
    }
}
