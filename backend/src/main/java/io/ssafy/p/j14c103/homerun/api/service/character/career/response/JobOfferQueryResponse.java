package io.ssafy.p.j14c103.homerun.api.service.character.career.response;

import io.ssafy.p.j14c103.homerun.domain.character.career.JobTransferPolicy;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import lombok.Getter;

@Getter
public class JobOfferQueryResponse {

    private final List<JobOfferResponse> offers;
    private final int offerChanceBonusRate;
    private final boolean meetFriendBonusApplied;

    private JobOfferQueryResponse(
        final List<JobOfferResponse> offers,
        final int offerChanceBonusRate,
        final boolean meetFriendBonusApplied
    ) {
        if (offers == null) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
        if (offers.stream().anyMatch(java.util.Objects::isNull)) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
        if (offerChanceBonusRate < 0) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
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

        return new JobOfferQueryResponse(
            jobOfferPool.offers().stream()
                .map(JobOfferResponse::from)
                .toList(),
            jobOfferPool.offerChanceBonusRate(),
            jobOfferPool.meetFriendBonusApplied()
        );
    }

    public List<JobOfferResponse> offers() {
        return offers;
    }

    public int offerChanceBonusRate() {
        return offerChanceBonusRate;
    }

    public boolean meetFriendBonusApplied() {
        return meetFriendBonusApplied;
    }

    @Getter
    public static class JobOfferResponse {

        private final String offerId;
        private final JobType jobType;
        private final String displayCompanyName;
        private final int currentSalary;
        private final int offeredSalary;
        private final Integer probationTurns;

        private JobOfferResponse(
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
            if (currentSalary <= 0) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }
            if (offeredSalary <= 0) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }
            if (probationTurns != null && probationTurns <= 0) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }

            this.offerId = offerId;
            this.jobType = jobType;
            this.displayCompanyName = displayCompanyName;
            this.currentSalary = currentSalary;
            this.offeredSalary = offeredSalary;
            this.probationTurns = probationTurns;
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

        public String offerId() {
            return offerId;
        }

        public JobType jobType() {
            return jobType;
        }

        public String displayCompanyName() {
            return displayCompanyName;
        }

        public int currentSalary() {
            return currentSalary;
        }

        public int offeredSalary() {
            return offeredSalary;
        }

        public Integer probationTurns() {
            return probationTurns;
        }
    }
}
