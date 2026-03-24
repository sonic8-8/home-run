package io.ssafy.p.j14c103.homerun.api.service.character.career;

import io.ssafy.p.j14c103.homerun.api.service.character.career.request.JobOfferQueryRequest;
import io.ssafy.p.j14c103.homerun.api.service.character.career.response.JobOfferQueryResponse;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobTransferPolicy;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import org.springframework.stereotype.Service;

@Service
public class JobOfferQueryService {

    private final JobTransferPolicy jobTransferPolicy;

    public JobOfferQueryService() {
        this(new JobTransferPolicy());
    }

    JobOfferQueryService(final JobTransferPolicy jobTransferPolicy) {
        if (jobTransferPolicy == null) {
            throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
        }

        this.jobTransferPolicy = jobTransferPolicy;
    }

    public JobOfferQueryResponse getJobOffers(final JobOfferQueryRequest request) {
        if (request == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }

        return JobOfferQueryResponse.from(jobTransferPolicy.calculateOfferPool(
            request.gameCareer(),
            request.gameStat(),
            request.recentMeetFriendCount()
        ));
    }
}
