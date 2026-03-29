package io.ssafy.p.j14c103.homerun.api.service.game.career.response;

import io.ssafy.p.j14c103.homerun.api.service.character.career.response.JobOfferQueryResponse;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class JobOfferListResponse {

    private final List<JobOfferResponse> offers;
    private final int offerChanceBonusRate;
    private final boolean meetFriendBonusApplied;

    @Builder(access = AccessLevel.PRIVATE)
    private JobOfferListResponse(
        final List<JobOfferResponse> offers,
        final int offerChanceBonusRate,
        final boolean meetFriendBonusApplied
    ) {
        this.offers = List.copyOf(offers);
        this.offerChanceBonusRate = offerChanceBonusRate;
        this.meetFriendBonusApplied = meetFriendBonusApplied;
    }

    public static JobOfferListResponse of(
        final List<JobOfferResponse> offers,
        final int offerChanceBonusRate,
        final boolean meetFriendBonusApplied
    ) {
        return JobOfferListResponse.builder()
            .offers(offers)
            .offerChanceBonusRate(offerChanceBonusRate)
            .meetFriendBonusApplied(meetFriendBonusApplied)
            .build();
    }

    public static JobOfferListResponse from(final JobOfferQueryResponse response) {
        if (response == null) {
            throw new HomerunException(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID);
        }
        return of(
            response.offers().stream()
                .map(JobOfferResponse::from)
                .toList(),
            response.offerChanceBonusRate(),
            response.meetFriendBonusApplied()
        );
    }

    @Getter
    public static class JobOfferResponse {

        private final String offerId;
        private final JobType jobType;
        private final String companyName;
        private final int currentSalary;
        private final int offeredSalary;
        private final Integer probationTurns;

        @Builder(access = AccessLevel.PRIVATE)
        private JobOfferResponse(
            final String offerId,
            final JobType jobType,
            final String companyName,
            final int currentSalary,
            final int offeredSalary,
            final Integer probationTurns
        ) {
            this.offerId = offerId;
            this.jobType = jobType;
            this.companyName = companyName;
            this.currentSalary = currentSalary;
            this.offeredSalary = offeredSalary;
            this.probationTurns = probationTurns;
        }

        public static JobOfferResponse of(
            final String offerId,
            final JobType jobType,
            final String companyName,
            final int currentSalary,
            final int offeredSalary,
            final Integer probationTurns
        ) {
            return JobOfferResponse.builder()
                .offerId(offerId)
                .jobType(jobType)
                .companyName(companyName)
                .currentSalary(currentSalary)
                .offeredSalary(offeredSalary)
                .probationTurns(probationTurns)
                .build();
        }

        private static JobOfferResponse from(
            final JobOfferQueryResponse.JobOfferResponse response
        ) {
            return of(
                response.offerId(),
                response.jobType(),
                response.displayCompanyName(),
                response.currentSalary(),
                response.offeredSalary(),
                response.probationTurns()
            );
        }
    }
}
