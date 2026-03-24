package io.ssafy.p.j14c103.homerun.api.service.character.career;

import io.ssafy.p.j14c103.homerun.api.service.character.career.request.JobTransferServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.character.career.response.JobTransferServiceResponse;
import io.ssafy.p.j14c103.homerun.api.service.character.history.GameplayHistoryWriter;
import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareer;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareerRepository;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobTitlePolicy;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobTransferPolicy;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class JobTransferService {

    private final GameCareerRepository gameCareerRepository;
    private final GameplayHistoryWriter gameplayHistoryWriter;
    private final JobTransferPolicy jobTransferPolicy = new JobTransferPolicy();
    private final JobTitlePolicy jobTitlePolicy = new JobTitlePolicy();

    public JobTransferServiceResponse transfer(final JobTransferServiceRequest request) {
        validateRequest(request);

        final GameCareer gameCareer = request.gameCareer();
        final GameplayHistoryWriter.CareerSnapshot previousCareer =
            GameplayHistoryWriter.CareerSnapshot.from(gameCareer);
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
        gameCareerRepository.save(gameCareer);
        final String transferMessage = buildTransferMessage(
            jobOffer.displayCompanyName(),
            previousEmploymentStatus
        );
        gameplayHistoryWriter.writeJobTransfer(
            previousCareer,
            gameCareer,
            request.currentTurn(),
            transferMessage
        );
        return JobTransferServiceResponse.of(
            previousJobType,
            gameCareer.getJobType(),
            gameCareer.getJobTitle(),
            gameCareer.getSalary(),
            gameCareer.getProbationEndTurn(),
            true,
            transferMessage
        );
    }

    private void validateRequest(final JobTransferServiceRequest request) {
        if (request == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
    }

    private String buildTransferMessage(
        final String companyName,
        final EmploymentStatus previousEmploymentStatus
    ) {
        if (previousEmploymentStatus == EmploymentStatus.UNEMPLOYED) {
            return companyName + "에 재취업했습니다.";
        }

        return companyName + "으로 이직했습니다.";
    }
}
