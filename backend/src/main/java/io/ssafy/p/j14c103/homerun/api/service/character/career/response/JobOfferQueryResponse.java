package io.ssafy.p.j14c103.homerun.api.service.character.career.response;

import io.ssafy.p.j14c103.homerun.domain.character.career.JobTransferPolicy;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class JobOfferQueryResponse {

    private final List<JobOfferResponse> offers;
    private final int offerChanceBonusRate;
    private final boolean meetFriendBonusApplied;

    @Builder(access = AccessLevel.PRIVATE)
    private JobOfferQueryResponse(
        final List<JobOfferResponse> offers,
        final int offerChanceBonusRate,
        final boolean meetFriendBonusApplied
    ) {
        validateRequest(offers, offerChanceBonusRate);

        this.offers = List.copyOf(offers);
        this.offerChanceBonusRate = offerChanceBonusRate;
        this.meetFriendBonusApplied = meetFriendBonusApplied;
    }

    public static JobOfferQueryResponse from(
        final JobTransferPolicy.JobOfferPool jobOfferPool
    ) {
        if (jobOfferPool == null) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }

        return JobOfferQueryResponse.builder()
            .offers(jobOfferPool.offers().stream()
                .map(JobOfferResponse::from)
                .toList())
            .offerChanceBonusRate(jobOfferPool.offerChanceBonusRate())
            .meetFriendBonusApplied(jobOfferPool.meetFriendBonusApplied())
            .build();
    }

    private void validateRequest(
        final List<JobOfferResponse> offers,
        final int offerChanceBonusRate
    ) {
        if (offers == null || offers.stream().anyMatch(Objects::isNull)) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
        if (offerChanceBonusRate < 0) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
    }

    @Getter
    public static class JobOfferResponse {

        private final String offerId;
        private final JobType jobType;
        private final String displayCompanyName;
        private final int currentSalary;
        private final int offeredSalary;
        private final Integer probationTurns;

        @Builder(access = AccessLevel.PRIVATE)
        private JobOfferResponse(
            final String offerId,
            final JobType jobType,
            final String displayCompanyName,
            final int currentSalary,
            final int offeredSalary,
            final Integer probationTurns
        ) {
            validateRequest(
                offerId,
                jobType,
                displayCompanyName,
                currentSalary,
                offeredSalary,
                probationTurns
            );

            this.offerId = offerId;
            this.jobType = jobType;
            this.displayCompanyName = displayCompanyName;
            this.currentSalary = currentSalary;
            this.offeredSalary = offeredSalary;
            this.probationTurns = probationTurns;
        }

        private static JobOfferResponse from(final JobTransferPolicy.JobOffer jobOffer) {
            return JobOfferResponse.builder()
                .offerId(jobOffer.offerId())
                .jobType(jobOffer.jobType())
                .displayCompanyName(jobOffer.displayCompanyName())
                .currentSalary(jobOffer.currentSalary())
                .offeredSalary(jobOffer.offeredSalary())
                .probationTurns(jobOffer.probationTurns())
                .build();
        }

        private void validateRequest(
            final String offerId,
            final JobType jobType,
            final String displayCompanyName,
            final int currentSalary,
            final int offeredSalary,
            final Integer probationTurns
        ) {
            if (offerId == null || offerId.isBlank()) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }
            if (jobType == null) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }
            if (displayCompanyName == null || displayCompanyName.isBlank()) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }
            if (currentSalary <= 0 || offeredSalary <= 0) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }
            if (probationTurns != null && probationTurns <= 0) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }
        }
    }
}
