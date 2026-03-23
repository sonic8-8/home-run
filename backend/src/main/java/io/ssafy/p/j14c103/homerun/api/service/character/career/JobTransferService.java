package io.ssafy.p.j14c103.homerun.api.service.character.career;

import io.ssafy.p.j14c103.homerun.api.service.character.career.request.JobTransferServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.character.career.response.JobTransferServiceResponse;
import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareer;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobTitlePolicy;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobTransferPolicy;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import org.springframework.stereotype.Service;

@Service
public class JobTransferService {

    private final JobTransferPolicy jobTransferPolicy;
    private final JobTitlePolicy jobTitlePolicy;

    public JobTransferService() {
        this(new JobTransferPolicy(), new JobTitlePolicy());
    }

    JobTransferService(
        final JobTransferPolicy jobTransferPolicy,
        final JobTitlePolicy jobTitlePolicy
    ) {
        if (jobTransferPolicy == null || jobTitlePolicy == null) {
            throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
        }

        this.jobTransferPolicy = jobTransferPolicy;
        this.jobTitlePolicy = jobTitlePolicy;
    }

    public JobTransferServiceResponse transfer(final JobTransferServiceRequest request) {
        if (request == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }

        final GameCareer gameCareer = request.gameCareer();
        final JobType previousJobType = gameCareer.getJobType();
        final EmploymentStatus previousEmploymentStatus = gameCareer.getEmploymentStatus();
        final JobTransferPolicy.JobOffer jobOffer = jobTransferPolicy.resolveOffer(
            request.gameCareer(),
            request.gameStat(),
            request.recentMeetFriendCount(),
            request.currentTurn(),
            request.offerId()
        );

        gameCareer.acceptTransfer(jobOffer, jobTitlePolicy, request.currentTurn());
        return JobTransferServiceResponse.of(
            previousJobType,
            gameCareer.getJobType(),
            gameCareer.getJobTitle(),
            gameCareer.getSalary(),
            gameCareer.getProbationEndTurn(),
            true,
            buildTransferMessage(jobOffer, previousEmploymentStatus)
        );
    }

    private String buildTransferMessage(
        final JobTransferPolicy.JobOffer jobOffer,
        final EmploymentStatus previousEmploymentStatus
    ) {
        if (previousEmploymentStatus == EmploymentStatus.UNEMPLOYED) {
            return jobOffer.displayCompanyName() + "에 재취업했습니다.";
        }

        return jobOffer.displayCompanyName() + "으로 이직했습니다.";
    }
}
